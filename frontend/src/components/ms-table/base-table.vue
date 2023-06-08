<template>
  <div class="ms-base-tale">
    <select-all
      v-if="attrs.showSelectAll"
      class="custom-action"
      :total="selectTotal"
      :current="selectCurrent"
      @change="handleChange"
    />
    <a-table v-bind="$attrs" :selected-keys="props.selectedKeys" @selection-change="selectionChange">
      <template v-for="(item, key, i) in slots" :key="i" #[key]="{ record, rowIndex, column }">
        <slot :name="key" v-bind="{ rowIndex, record, column }"></slot>
      </template>
      <template v-if="selectCurrent > 0" #pagination-left>
        <batch-action
          :select-row-count="selectCurrent"
          @batch-export="emit('batchExport')"
          @batch-edit="emit('batchEdit')"
          @batch-move="emit('batchMove')"
          @batch-related="emit('batchRelated')"
          @batch-delete="emit('batchDelete')"
        />
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
  // eslint-disable no-console
  import { useSlots, useAttrs, computed, ref, onMounted, watchEffect } from 'vue';
  import selectAll from './select-all.vue';
  import { MsTabelProps, SelectAllEnum, MsPaginationI } from './type';
  import BatchAction from './batchAction.vue';

  const batchleft = ref('10px');
  const props = defineProps({
    selectedKeys: {
      type: Array as unknown as () => (string | number)[],
      default: () => [],
    },
  });
  const emit = defineEmits<{
    (e: 'selectedChange', value: (string | number)[]): void;
    (e: 'batchExport'): void;
    (e: 'batchEdit'): void;
    (e: 'batchMove'): void;
    (e: 'batchRelated'): void;
    (e: 'batchDelete'): void;
  }>();
  const isSelectAll = ref(false);
  const selectCurrent = ref(0);

  const slots = useSlots();
  const attrs = useAttrs();

  const { data, rowKey, pagination }: Partial<MsTabelProps> = attrs;

  const selectTotal = computed(() => {
    if (pagination) {
      const { pageSize } = pagination as MsPaginationI;
      return pageSize;
    }
    return data?.length || 20;
  });
  // 选择行change事件
  const selectionChange = (arr: (string | number)[]) => {
    emit('selectedChange', arr);
  };

  watchEffect(() => {
    selectCurrent.value = props.selectedKeys.length;
  });

  // 全选change事件
  const handleChange = (v: string) => {
    isSelectAll.value = v === SelectAllEnum.ALL;
    if (v === SelectAllEnum.NONE) {
      selectionChange([]);
      selectCurrent.value = 0;
    } else {
      if (data && data.length > 0) {
        selectionChange(data.map((item: any) => item[rowKey || 'id']));
      }
      if (v === SelectAllEnum.ALL) {
        selectCurrent.value = selectTotal.value;
      }
    }
  };

  // 根据参数获取全选按钮的位置
  const getBatchLeft = () => {
    if (attrs.enableDrag) {
      return '30px';
    }
    switch (attrs.size) {
      case 'small':
        return '10px';
      case 'mini':
        return '10px';
      default:
        return '10px';
    }
  };

  onMounted(() => {
    batchleft.value = getBatchLeft();
  });
</script>

<style lang="less" scoped>
  .ms-base-tale {
    position: relative;
    .custom-action {
      position: absolute;
      top: 3px;
      left: v-bind(batchleft);
      z-index: 100;
      border-radius: 2px;
      line-height: 40px;
      cursor: pointer;
    }
  }
</style>
