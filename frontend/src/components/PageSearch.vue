<script setup>
import { ElInput, ElSelect, ElOption, ElButton, ElPagination } from 'element-plus'

const props = defineProps({
  searchForm: {
    type: Object,
    default: () => ({})
  },
  searchFields: {
    type: Array,
    default: () => []
  },
  pageInfo: {
    type: Object,
    default: () => ({ page: 1, pageSize: 10, total: 0 })
  }
})

const emit = defineEmits(['search', 'pageChange', 'sizeChange'])

const handleSearch = () => {
  emit('search', { ...props.searchForm })
}

const handleReset = () => {
  const resetForm = {}
  props.searchFields.forEach(field => {
    resetForm[field.prop] = ''
  })
  emit('search', resetForm)
}

const handlePageChange = (page) => {
  emit('pageChange', page)
}

const handleSizeChange = (size) => {
  emit('sizeChange', size)
}
</script>

<template>
  <div class="page-search">
    <div class="search-form">
      <template v-for="field in searchFields" :key="field.prop">
        <ElInput
          v-if="field.type === 'input'"
          :placeholder="field.placeholder"
          v-model="searchForm[field.prop]"
          clearable
          class="search-input"
        />
        <ElSelect
          v-else-if="field.type === 'select'"
          :placeholder="field.placeholder"
          v-model="searchForm[field.prop]"
          clearable
          class="search-input"
        >
          <ElOption
            v-for="option in field.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </ElSelect>
      </template>
      <div class="search-actions">
        <ElButton type="primary" @click="handleSearch">搜索</ElButton>
        <ElButton @click="handleReset">重置</ElButton>
      </div>
    </div>
    <div class="pagination-wrapper">
      <ElPagination
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        :current-page="pageInfo.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageInfo.pageSize"
        :total="pageInfo.total"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
  </div>
</template>

<style scoped>
.page-search {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  align-items: center;
  margin-bottom: 15px;
}

.search-input {
  width: 200px;
}

.search-actions {
  display: flex;
  gap: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
}
</style>