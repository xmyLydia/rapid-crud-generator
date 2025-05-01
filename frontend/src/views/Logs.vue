<template>
  <div style="max-width: 900px; margin: auto" v-loading="loading">
    <h2 style="margin: 1rem 0">Log Search Result (Total: {{ total }})</h2>

    <!-- search -->
    <el-form inline @submit.prevent style="margin-bottom: 20px;">
      <el-form-item label="Keyword">
        <el-input v-model="keyword" placeholder="Keyword..." clearable />
      </el-form-item>

      <el-form-item label="Action">
        <el-input v-model="action" placeholder="e.g. GENERATE" clearable />
      </el-form-item>

      <el-form-item label="Entity">
        <el-input v-model="entity" placeholder="e.g. User" clearable />
      </el-form-item>

      <el-form-item label="Sort">
        <el-select v-model="sortOrder" placeholder="Order" style="width: 130px;" @change="search">
          <el-option label="Newest First" value="DESC" />
          <el-option label="Oldest First" value="ASC" />
        </el-select>
      </el-form-item>


      <el-form-item>
        <el-button type="primary" :loading="loading" @click="search">Search</el-button>
        <el-button type="success" @click="exportCsv" :disabled="logs.length === 0">Export CSV</el-button>
      </el-form-item>
    </el-form>

    <!-- log -->
    <el-card
      v-for="(item, index) in logs"
      :key="index"
      style="margin-bottom: 20px;"
      shadow="hover"
    >
      <p><strong>Time:</strong> {{ item.timestamp }}</p>
      <p><strong>Action:</strong> <span v-html="item.highlighted_action || item.action"></span></p>

      <!-- expandable payload -->
      <el-collapse>
        <el-collapse-item title="Payload (raw)" name="payload">
          <pre><code>{{ item.payload }}</code></pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- pagination -->
    <div style="text-align: center; margin-top: 20px;">
      <el-pagination
        background
        layout="prev, pager, next"
        :page-size="size"
        :current-page="page + 1"
        :total="total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const keyword = ref('')
const action = ref('')
const entity = ref('')
const sortOrder = ref('DESC')

const page = ref(0)
const size = ref(5)
const total = ref(0)
const logs = ref([])
const loading = ref(false)

const fetchLogs = async () => {
  loading.value = true
  const response = await fetch('/api/logs/elasticsearch/search', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      action: action.value,
      entity: entity.value,
      keyword: keyword.value,
      page: page.value,
      size: size.value,
      sortBy: 'timestamp',
      sortOrder: sortOrder.value,
      highlightFields: ['payload', 'action']
    })
  })
  const json = await response.json()
  logs.value = json.data
  total.value = json.total
  loading.value = false
}

const search = () => {
  page.value = 0
  fetchLogs()
}

const handlePageChange = (newPage) => {
  page.value = newPage - 1
  fetchLogs()
}

const exportCsv = () => {
  const headers = ['timestamp', 'action', 'entity', 'payload']
  const rows = logs.value.map(item => [
    item.timestamp,
    item.action,
    item.entity || '',
    (item.payload || '').replace(/\n/g, ' ')
  ])

  const csvContent = [headers, ...rows]
    .map(row => row.map(field => `"${(field + '').replace(/"/g, '""')}"`).join(','))
    .join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.setAttribute('download', 'audit_logs.csv')
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

fetchLogs()
</script>

<style>
em {
  background-color: yellow;
  font-style: normal;
}
</style>
