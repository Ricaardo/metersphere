<template>
  <div class="ms-header">
    <el-row type="flex" class="head-bar">
      <el-col :span="10">
        <div class="name-edit">
          <span class="title">{{ title }}</span>
        </div>
      </el-col>
      <el-col :span="14">
        <div class="ms-export">
          <el-dropdown style="padding-left: 3rem">
            <span class="el-dropdown-link">
              <i class="el-icon-download"/>
              {{ $t('commons.export') }}
            </span>
            <el-dropdown-menu slot="dropdown" class="drawer-header">
              <el-dropdown-item @click.native.stop="exportJPG">JPEG</el-dropdown-item>
              <el-dropdown-item @click.native.stop="exportPNG">PNG</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <i class="delimiter">|</i>
          <el-button size="mini" icon="el-icon-close" @click="close" class="close-btn" style="float: right; margin: 0.3rem 1.2rem 0 0"></el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "DrawerHeader",
  components: {},
  data() {
    return {}
  },
  props: {
    title: String,
    buttons: []
  },
  methods: {
    exportJPG() {
      this.$emit("export","JPEG")
    },
    exportPNG() {
      this.$emit("export","PNG")
    },
    close() {
      this.$emit('close');
    },
  },
}
</script>

<style scoped>
.name-edit {
  padding-left: 10px;
}

.head-bar {
  background: white;
  height: 45px;
  line-height: 45px;
  border: 1px solid #EBEEF5;
  box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
}

.head-right {
  text-align: right;
}

.title {
  margin-left: 10px;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: #1F2329;
  flex: none;
  order: 1;
  flex-grow: 0;
}

button.el-button.el-button--default.el-button--mini {
  border: none!important;
}

:deep(.el-icon-close:before) {
  font-size: 20px;
}

span.el-dropdown-link.el-dropdown-selfdefine {
  width: 56px;
  height: 26px;
  border-radius: 4px;
  cursor: pointer;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  display: flex;
  align-items: center;
  letter-spacing: -0.1px;
  color: #1F2329;
  flex: none;
  order: 1;
  flex-grow: 0;
  position: relative;
  padding-left: 15px;
}

span.el-dropdown-link.el-dropdown-selfdefine:hover {
  background: rgba(31, 35, 41, 0.1);
}

i.el-icon-download {
  margin-right: 4px;
  font-size: 17px;
}

.el-icon-download:before {
  margin-left: -5px;
}

i.delimiter {
  width: 1px;
  height: 20px;
  color: #BBBFC4;
  /* margin-top: -5px; */
  position: relative;
  top: -2px;
  left: 15px;
  padding-right: 10px;
}

.ms-export {
  float: right;
}

.close-btn {
  background-color: transparent!important;
}
</style>

<style>
.drawer-header .el-dropdown-menu__item {
  color: #1F2329 !important;
}

.drawer-header .el-dropdown-menu__item:hover {
  background-color: rgba(31, 35, 41, 0.1) !important;
  color: #1F2329 !important;
}
</style>
