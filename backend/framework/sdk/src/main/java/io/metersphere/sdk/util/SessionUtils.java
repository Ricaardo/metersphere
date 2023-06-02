package io.metersphere.sdk.util;

import io.metersphere.sdk.constants.UserRoleConstants;
import io.metersphere.sdk.dto.SessionUser;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.domain.UserRolePermission;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.sdk.constants.SessionConstants.ATTR_USER;

public class SessionUtils {

    private static final ThreadLocal<String> projectId = new ThreadLocal<>();
    private static final ThreadLocal<String> workspaceId = new ThreadLocal<>();

    public static String getUserId() {
        SessionUser user = getUser();
        return user == null ? null : user.getId();
    }

    public static SessionUser getUser() {
        try {
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession();
            return (SessionUser) session.getAttribute(ATTR_USER);
        } catch (Exception e) {
            LogUtils.warn("后台获取在线用户失败: " + e.getMessage());
            return null;
        }
    }

    public static String getSessionId() {
        return (String) SecurityUtils.getSubject().getSession().getId();
    }

    /**
     * 踢除用户
     *
     * @param username
     */
    public static void kickOutUser(String username) {
        // redis session
        RedisIndexedSessionRepository sessionRepository = CommonBeanFactory.getBean(RedisIndexedSessionRepository.class);
        if (sessionRepository == null) {
            return;
        }
        Map<String, ?> users = sessionRepository.findByPrincipalName(username);
        if (MapUtils.isNotEmpty(users)) {
            users.keySet().forEach(k -> {
                sessionRepository.deleteById(k);
                sessionRepository.getSessionRedisOperations().delete("spring:session:sessions:" + k);
            });
        }
    }

    //
    public static void putUser(SessionUser sessionUser) {
        SecurityUtils.getSubject().getSession().setAttribute(ATTR_USER, sessionUser);
        SecurityUtils.getSubject().getSession().setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, sessionUser.getId());
    }

    /**
     * 权限验证时从 controller 参数列表中找到 workspaceId 传入
     */
    public static void setCurrentWorkspaceId(String workspaceId) {
        SessionUtils.workspaceId.set(workspaceId);
    }

    /**
     * 权限验证时从 controller 参数列表中找到 projectId 传入
     */
    public static void setCurrentProjectId(String projectId) {
        SessionUtils.projectId.set(projectId);
    }

    public static String getCurrentWorkspaceId() {
        if (StringUtils.isNotEmpty(workspaceId.get())) {
            return workspaceId.get();
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            LogUtils.debug("WORKSPACE: {}", request.getHeader("WORKSPACE"));
            if (request.getHeader("WORKSPACE") != null) {
                return request.getHeader("WORKSPACE");
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
        return getUser().getLastWorkspaceId();
    }

    public static String getCurrentProjectId() {
        if (StringUtils.isNotEmpty(projectId.get())) {
            return projectId.get();
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            LogUtils.debug("PROJECT: {}", request.getHeader("PROJECT"));
            if (request.getHeader("PROJECT") != null) {
                return request.getHeader("PROJECT");
            }
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
        }
        return getUser().getLastProjectId();
    }

    public static String getHttpHeader(String headerName) {
        if (StringUtils.isBlank(headerName)) {
            return null;
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader(headerName);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasPermission(String workspaceId, String projectId, String permission) {
        Map<String, List<UserRolePermission>> userRolePermissions = new HashMap<>();
        Map<String, UserRole> role = new HashMap<>();
        SessionUser user = Objects.requireNonNull(SessionUtils.getUser());
        user.getUserRoleRelations().forEach(ug -> user.getUserRolePermissions().forEach(gp -> {
            if (StringUtils.equals(gp.getUserRole().getId(), ug.getRoleId())) {
                userRolePermissions.put(ug.getId(), gp.getUserRolePermissions());
                role.put(ug.getId(), gp.getUserRole());
            }
        }));

        long count = user.getUserRoles()
                .stream()
                .filter(g -> StringUtils.equals(g.getId(), UserRoleConstants.SUPER_ROLE))
                .count();

        if (count > 0) {
            return true;
        }


        Set<String> currentProjectPermissions = getCurrentProjectPermissions(userRolePermissions, projectId, role, user);
        if (currentProjectPermissions.contains(permission)) {
            return true;
        }

        Set<String> currentWorkspacePermissions = getCurrentWorkspacePermissions(userRolePermissions, workspaceId, role, user);
        if (currentWorkspacePermissions.contains(permission)) {
            return true;
        }

        Set<String> systemPermissions = getSystemPermissions(userRolePermissions, role, user);
        return systemPermissions.contains(permission);
    }

    private static Set<String> getSystemPermissions(Map<String, List<UserRolePermission>> userRolePermissions, Map<String, UserRole> role, SessionUser user) {
        return user.getUserRoleRelations().stream()
                .filter(ug -> role.get(ug.getId()) != null && StringUtils.equals(role.get(ug.getId()).getType(), "SYSTEM"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), "system") || StringUtils.equals(ug.getSourceId(), "'adminSourceId'"))
                .flatMap(ug -> userRolePermissions.get(ug.getId()).stream())
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    private static Set<String> getCurrentWorkspacePermissions(Map<String, List<UserRolePermission>> userRolePermissions, String workspaceId, Map<String, UserRole> role, SessionUser user) {
        return user.getUserRoleRelations().stream()
                .filter(ug -> role.get(ug.getId()) != null && StringUtils.equals(role.get(ug.getId()).getType(), "WORKSPACE"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), workspaceId))
                .flatMap(ug -> userRolePermissions.get(ug.getId()).stream())
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    private static Set<String> getCurrentProjectPermissions(Map<String, List<UserRolePermission>> userRolePermissions, String projectId, Map<String, UserRole> role, SessionUser user) {
        return user.getUserRoleRelations().stream()
                .filter(ug -> role.get(ug.getId()) != null && StringUtils.equals(role.get(ug.getId()).getType(), "PROJECT"))
                .filter(ug -> StringUtils.equals(ug.getSourceId(), projectId))
                .flatMap(ug -> userRolePermissions.get(ug.getId()).stream())
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    public static void clearCurrentWorkspaceId() {
        workspaceId.remove();
    }

    public static void clearCurrentProjectId() {
        projectId.remove();
    }
}
