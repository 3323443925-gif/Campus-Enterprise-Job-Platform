<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  type: {
    type: String,
    default: 'bar'
  },
  data: {
    type: Object,
    default: () => ({})
  },
  title: {
    type: String,
    default: ''
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '300px'
  }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!chartInstance) return

  let option = {}

  if (props.type === 'bar') {
    option = {
      title: {
        text: props.title,
        left: 'center'
      },
      xAxis: {
        type: 'category',
        data: props.data.xData || [],
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      },
      series: [{
        data: props.data.seriesData || [],
        type: 'bar',
        barMinHeight: 1
      }]
    }
  } else if (props.type === 'pie') {
    option = {
      title: {
        text: props.title,
        left: 'center'
      },
      series: [{
        type: 'pie',
        data: props.data.seriesData || []
      }]
    }
  } else if (props.type === 'line') {
    option = {
      title: {
        text: props.title,
        left: 'center'
      },
      xAxis: {
        type: 'category',
        data: props.data.xData || []
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: props.data.seriesData || [],
        type: 'line'
      }]
    }
  }

  chartInstance.setOption(option, true)
}

const handleResize = () => {
  chartInstance?.resize()
}

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<template>
  <div
      ref="chartRef"
      :style="{ width: width, height: height }"
      class="echarts-box"
  ></div>
</template>

<style scoped>
.echarts-box {
  background: #fff;
  border-radius: 8px;
  padding: 10px;
}
</style>