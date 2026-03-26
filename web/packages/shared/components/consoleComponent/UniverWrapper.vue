<template>
  <div class="univer-container">
    <div ref="univerContainer" class="univer-sheet"></div>
    <!-- Pagination Controls -->
    <div v-if="showPaginationControls" class="pagination-controls">
      <Button 
        :disabled="currentPage <= 0" 
        @click="goToPage(currentPage - 1)"
        type="text"
      >
        {{
          $t('message.common.prePage')
        }}
      </Button>
      <span class="page-info">
        {{
          $t('message.common.No_')
        }} {{ currentPage + 1 }} {{
          $t('message.common.pageTotal')
        }} {{ totalPages }} {{
          $t('message.common.page')
        }}
      </span>
      <Button 
        :disabled="currentPage >= totalPages - 1" 
        @click="goToPage(currentPage + 1)"
        type="text"
      >
        {{
          $t('message.common.nextPage')
        }}
      </Button>
      <span class="page-size-info">{{
          $t('message.common.perPage')
        }} {{ pageSize }} {{
          $t('message.common.line')
        }}</span>
      <span class="page-size-info" v-if="showTotalRowTips">{{
          $t('message.common.only500')
        }}</span>
    </div>
  </div>
</template>

<script>
import api from '@dataspherestudio/shared/common/service/api';
import _ from "lodash";


export default {
  name: 'UniverWrapper',
  props: {
    script: {
      type: Object,
      required: true,
    },
    scriptViewState: {
      type: Object,
      required: true
    },
    height: {
      type: Number,
      default: 400
    },
    fetchData: {
      type: Function,
      required: true
    }
  },
  data() {
    return {
      univerAPI: null,
      univerInstance: null,
      disposableSheetChange: null,
      sheetIdPrefix: 'curResultSheet',
      loadedResultIndices: new Set(),
      // 将 Map 改为响应式对象
      paginationInfo: {},
      pageSize: 1000,
      pageSizeOpts: [20, 50, 100, 500,1000],
      currentActiveSheetIndex: 0,
      // 添加响应式属性跟踪当前页
      currentPageNumber: 0,
      showTotalRowTips: false,
    }
  },
  mounted() {
    console.log('script', this.script)
    this.loadCDNScripts()
    // 监听父组件的重新加载数据事件
    this.$parent.$on('reload-univer-data', this.reloadUniverData);
  },
  beforeUnmount() {
    if (this.univerInstance) {
      this.univerInstance.dispose()
    }
    if (this.univerAPI) {
      this.univerAPI.dispose()
    }
    this.univerInstance = null
    this.univerAPI = null
    if (this.disposableSheetChange) {
      this.disposableSheetChange.dispose()
    }
    // 移除事件监听
    if (this.$parent) {
      this.$parent.$off('reload-univer-data', this.reloadUniverData);
    }
  },
  methods: {
    async loadCDNScripts() {
      if (typeof window.UniverPresets === 'undefined') {
        await this.loadScript('/univerCDN/lib/js/react.production.min.js')
        await this.loadScript('/univerCDN/lib/js/react-dom.production.min.js')
        await this.loadScript('/univerCDN/lib/js/rxjs.umd.min.js')
        await this.loadScript('/univerCDN/lib/js/echarts.min.js')
        await this.loadScript('/univerCDN/lib/js/univerjs-preset.js')
        await this.loadScript('/univerCDN/lib/js/univerjs-sheets-core.js')
        await this.loadScript('/univerCDN/lib/js/univerjs-sheets-sort.js')
        await this.loadScript('/univerCDN/lib/js/univerjs-sheets-sort-zh-CN.js')
        await this.loadScript('/univerCDN/lib/js/univerjs-sheets-zh-CN.js')
        
        await this.loadCSS('/univerCDN/lib/css/univerjs-sheets-core.css')
        await this.loadCSS('/univerCDN/lib/css/univerjs-sheets-sort.css')
      }
      
      this.initUniver()
    },
    loadScript(src) {
      return new Promise((resolve, reject) => {
        const script = document.createElement('script')
        script.src = src
        script.onload = resolve
        script.onerror = reject
        document.head.appendChild(script)
      })
    },
    loadCSS(href) {
      return new Promise((resolve, reject) => {
        const link = document.createElement('link')
        link.rel = 'stylesheet'
        link.href = href
        link.onload = resolve
        link.onerror = reject
        document.head.appendChild(link)
      })
    },
    convertToBodyRows(fileContent) {
      return fileContent;
    },
    overwriteSheetData(worksheet, data) {
      try {
        const rowCount = data.length;
        const colCount = data[0] ? data[0].length : 0;
        
        if (rowCount === 0 || colCount === 0) {
          return;
        }
        
        // 确保工作表有足够的行列
        worksheet.setRowCount(rowCount);
        worksheet.setColumnCount(colCount);
        
        const range = worksheet.getRange(0, 0, rowCount, colCount);
        range.setValues(data);
        
      } catch (error) {
        console.error('Failed to overwrite sheet data:', error);
      }
    },
    async handleSheetChange(params) {
      const { workbook, activeSheet } = params;
      const regex = new RegExp(`^${this.sheetIdPrefix}(\\d+)$`);
      const match = activeSheet.getSheetId().match(regex);
      
      if (match) {
        const number = parseInt(match[1], 10);
        const index = number - 1;
        
        this.currentActiveSheetIndex = index;
        
        // 更新当前页码显示
        const pagination = this.paginationInfo[index] || { currentPage: 0 };
        this.currentPageNumber = pagination.currentPage;
        
        if(this.script && this.script.resultList.length > 0 && this.script.resultList[index]){
          const curResult = this.script.resultList[index];

          // 同步列分页状态：更新scriptViewState.columnPageNow以匹配当前结果集
          // 如果结果集有自己的列分页状态，则使用该状态；否则重置为第1页
          // const targetColumnPage = curResult.result && curResult.result.columnPageNow ? curResult.result.columnPageNow : 1;
          // this.$emit('update-column-page', targetColumnPage);

          // 调整成每次切换结果集都请求数据，确保提示会刷新
          // if (this.loadedResultIndices.has(index)) {
          //   if (curResult.result) {
          //     await this.loadDataForSheet(activeSheet, curResult.result, index);
          //   }
          // } else {
            // 调用父组件的数据获取方法
            const processedData = await this.fetchData(index, curResult.path);
            if (processedData) {
              console.log(`Univer接收数据: 结果集${index}, 实际列数=${processedData.headRows ? processedData.headRows.length : 0}, 总行数=${processedData.bodyRows ? processedData.bodyRows.length : 0}`);
              this.script.resultList[index].result = processedData;
              this.loadedResultIndices.add(index);
              
              // 初始化分页信息
              this.$set(this.paginationInfo, index, {
                totalRows: processedData.bodyRows ? processedData.bodyRows.length : 0,
                currentPage: 0,
                totalPages: processedData.bodyRows ? Math.ceil(processedData.bodyRows.length / this.pageSize) : 1
              });

               // 同步列分页状态：新获取的数据也要同步列分页
              // const targetColumnPage = processedData.columnPageNow || 1;
              // this.$emit('update-column-page', targetColumnPage);

              await this.loadDataForSheet(activeSheet, processedData, index);
            }
          // }
        }
      }
    },
    async loadDataForSheet(worksheet, data, index) {
      let displayData = _.cloneDeep(data);
      const pagination = this.paginationInfo[index] || { currentPage: 0 };
      
      // 应用行分页：如果数据量超过每页大小，只显示当前页数据
      if (displayData.bodyRows && displayData.bodyRows.length > this.pageSize) {
        const startRow = pagination.currentPage * this.pageSize;
        const endRow = Math.min(startRow + this.pageSize, displayData.bodyRows.length);
        const paginatedBodyRows = displayData.bodyRows.slice(startRow, endRow);
        
        displayData = {
          ...displayData,
          bodyRows: paginatedBodyRows
        };
        
        console.log(`Univer行分页: 总数据${data.bodyRows.length}行，显示第${startRow + 1}-${endRow}行（第${pagination.currentPage + 1}页）`);
      }
        
      const curCellData = this.handleSimpleCellData(displayData.headRows, displayData.bodyRows);
      if(data.bodyRows.length >= 5000){
        this.showTotalRowTips = true;
      } else {
        this.showTotalRowTips = false;
      }
      this.overwriteSheetData(worksheet, curCellData);
    },
    async initUniver() {
      if (typeof window.UniverPresets === 'undefined') {
        console.warn('Univer dependencies not loaded yet')
        return
      }
      
      if (this.$refs.univerContainer) {
        this.$refs.univerContainer.style.height = `${this.height}px`
      }
      
      try {
        const { createUniver } = window.UniverPresets
        const { LocaleType, mergeLocales } = window.UniverCore
        const { UniverSheetsCorePreset } = window.UniverPresetSheetsCore
        const zhCNLocales = window.UniverPresetSheetsCoreZhCN
        const { UniverSheetsSortPreset } = window.UniverPresetSheetsSort
        const sortZhCNLocales = window.UniverPresetSheetsSortZhCN

        const { univer, univerAPI } = createUniver({
          locale: LocaleType.ZH_CN,
          locales: {
            [LocaleType.ZH_CN]: mergeLocales(zhCNLocales, sortZhCNLocales),
          },
          presets: [
            UniverSheetsCorePreset({
              container: this.$refs.univerContainer,
            }),
            UniverSheetsSortPreset()
          ],
          renderEngine: 'canvas',
          virtualization: {
            horizontal: {
              enabled: true,
              bufferSize: 20,
            },
            vertical: {
              enabled: true,
              bufferSize: 20,
            },
          },
        })
        
        this.univerInstance = univer
        this.univerAPI = univerAPI
        
        // 构建表格对象
        const sheetObj = {};
        
        for (let index = 0; index < this.script.resultList.length; index++) {
          const resultItem = this.script.resultList[index];
          let curData = resultItem.result;
          
          // 初始化分页信息
          if (curData && curData.bodyRows) {
            this.$set(this.paginationInfo, index, {
              totalRows: curData.bodyRows.length,
              currentPage: 0,
              totalPages: Math.ceil(curData.bodyRows.length / this.pageSize)
            });
          }
          
          let displayData = curData;
          
          // 应用行分页到初始化数据（只显示第一页）
          if (curData && curData.bodyRows && curData.bodyRows.length > this.pageSize) {
            const paginatedBodyRows = curData.bodyRows.slice(0, this.pageSize);
            displayData = {
              ...curData,
              bodyRows: paginatedBodyRows
            };
            
            console.log(`Univer初始化行分页: 总数据${curData.bodyRows.length}行，初始化显示前${this.pageSize}行`);
          }
          
            
          const cellData = displayData ? this.handleCellData(displayData) : {};
          
          const curSheet = {
            id: `${this.sheetIdPrefix}${index + 1}`,
            cellData,
            name: `${this.$t('message.common.resultList')}${index + 1}`,
            hidden: 0,
            rowCount: displayData && displayData.bodyRows ? displayData.bodyRows.length + 1 : 1,
            columnCount: displayData && displayData.headRows ? displayData.headRows.length : 1,
          };
          
          sheetObj[`${this.sheetIdPrefix}${index + 1}`] = curSheet;
        }
        
        // 创建工作簿
        const workbookData = {
          id: 'workbook-01',
          name: 'universheet',
          sheets: sheetObj,
        };
        
        const workbook = univerAPI.createWorkbook(workbookData)
        const workSheetList = workbook.getSheets();
        const defaultRowStyle = {
          bg: {
            rgb: '#5E9DE0',
          },
        }
        
        workSheetList.forEach((iSheet) => {
          iSheet.setRowDefaultStyle(0, defaultRowStyle)
          iSheet.setFrozenRows(1)
        })
        
        // 启用sheet切换事件监听
        this.disposableSheetChange = univerAPI.addEvent(univerAPI.Event.ActiveSheetChanged, (params) => {
          this.handleSheetChange(params)
        });
        
      } catch (error) {
        console.error('Failed to initialize Univer:', error)
      }
    },
    handleSimpleCellData(columnData, rowData) {
      const totalColumns = columnData && columnData.length || 0;
      const cellValue = _.cloneDeep(rowData) || [];
      const firstRow = []
      
      for (let i = 0; i < totalColumns; i++) {
        const headerCell = columnData[i];
        firstRow[i] = headerCell && headerCell.columnName || '';
      }
      
      cellValue.unshift(firstRow);
      return cellValue
    },
    handleCellData(data) {
      const CHUNK_SIZE = 500;
      const totalRows =  data && data.bodyRows && data.bodyRows.length || 0;
      const totalColumns = data && data.headRows && data.headRows.length || 0;
      const cellValue = {
        0: {}
      };
      for (let i = 0; i < totalColumns; i++) {
        const headerCell = data.headRows[i];
        // 假设每个表头对象有 `title` 字段
        cellValue[0][i] = { v: headerCell && headerCell.columnName || '' };
      }
      // 添加一下每行列名
      for (let chunkStart = 0; chunkStart <= totalRows; chunkStart += CHUNK_SIZE) {
        const chunkEnd = Math.min(chunkStart + CHUNK_SIZE, totalRows);
        for (let dataIndex = chunkStart; dataIndex <= chunkEnd; dataIndex++) {
          const rowIndex = dataIndex + 1; //  映射：dataIndex=0 → rowIndex=1
          const row = data.bodyRows[dataIndex];
          if (!cellValue[rowIndex]) {
            cellValue[rowIndex] = {};
          }
          if (typeof row === 'object' && row !== null) {
            const rowValues = Object.values(row);
            rowValues.forEach((cell, colIndex) => {
              cellValue[rowIndex][colIndex] = { v: cell };
            });
          }
        }
        // console.log(`Processed data rows ${chunkStart} to ${chunkEnd - 1}, mapped to output rows ${chunkStart + 1} to ${chunkEnd}`);
      }
      return cellValue
    },
    // async refreshCurrentSheetData() {
    //   if (!this.univerAPI || !this.script || this.script.resultList.length === 0) {
    //     return;
    //   }
      
    //   const index = this.currentActiveSheetIndex;
    //   if (index < 0 || index >= this.script.resultList.length) {
    //     return;
    //   }
      
    //   const workbook = this.univerAPI.getWorkbook('workbook-01');
    //   if (!workbook) {
    //     return;
    //   }
      
    //   const sheetId = `${this.sheetIdPrefix}${index + 1}`;
    //   const worksheet = workbook.getSheetBySheetId(sheetId);
      
    //   if (!worksheet) {
    //     return;
    //   }
      
    //   // Clear the cache for this result to force fresh data fetch
    //   this.loadedResultIndices.delete(index);
      
    //   // Get fresh data with the new column page
    //   const curResult = this.script.resultList[index];
    //   const processedData = await this.fetchData(index, curResult.path);
      
    //   if (processedData) {
    //     // Update the result data
    //     this.script.resultList[index].result = processedData;
    //     this.loadedResultIndices.add(index);
        
    //     // Update pagination info
    //     this.$set(this.paginationInfo, index, {
    //       totalRows: processedData.bodyRows ? processedData.bodyRows.length : 0,
    //       currentPage: 0,
    //       totalPages: processedData.bodyRows ? Math.ceil(processedData.bodyRows.length / this.pageSize) : 1
    //     });
        
    //     // Reload the sheet data
    //     await this.loadDataForSheet(worksheet, processedData, index);
    //   }
    // },
    async goToPage(pageNumber) {
      const index = this.currentActiveSheetIndex;
      const pagination = this.paginationInfo[index];
      
      if (!pagination || pageNumber < 0 || pageNumber >= pagination.totalPages) {
        return;
      }
      
      // 更新当前页码
      this.$set(this.paginationInfo, index, {
        ...pagination,
        currentPage: pageNumber
      });
      
      // 更新响应式页码
      this.currentPageNumber = pageNumber;
      
      // 重新加载当前sheet的数据
      if (this.univerAPI && this.script && this.script.resultList[index]) {
        const workbook = this.univerAPI.getWorkbook('workbook-01');
        if (workbook) {
          const sheetId = `${this.sheetIdPrefix}${index + 1}`;
          const worksheet = workbook.getSheetBySheetId(sheetId);
          
          if (worksheet && this.script.resultList[index].result) {
            const curResult = this.script.resultList[index];
            await this.loadDataForSheet(worksheet, curResult.result, index);
          }
        }
      }
    },
    // 轻量级重新加载Univer数据 - 只更新首个结果集数据
    async reloadUniverData() {
      // console.log('Light reloading Univer data...');
      // 只更新首个结果集（索引0）的数据
      const index = 0;
      if (this.script && this.script.resultList && this.script.resultList[index]) {
        // 清除该结果集的缓存标记
        this.loadedResultIndices.delete(index);
        
        // 获取工作簿和工作表
        if (this.univerAPI) {
          const workbook = this.univerAPI.getWorkbook('workbook-01');
          if (workbook) {
            const sheetId = `${this.sheetIdPrefix}${index + 1}`;
            const worksheet = workbook.getSheetBySheetId(sheetId);
            
            if (worksheet) {
              // 重新获取数据并更新
              const curResult = this.script.resultList[index];
              const processedData = await this.fetchData(index, curResult.path);
              if (processedData) {
                this.script.resultList[index].result = processedData;
                this.loadedResultIndices.add(index);
                
                // 更新分页信息
                this.$set(this.paginationInfo, index, {
                  totalRows: processedData.bodyRows ? processedData.bodyRows.length : 0,
                  currentPage: 0,
                  totalPages: processedData.bodyRows ? Math.ceil(processedData.bodyRows.length / this.pageSize) : 1
                });
                
                // 更新工作表数据
                await this.loadDataForSheet(worksheet, processedData, index);
              }
            }
            worksheet.setFrozenRows(1);
          }
        }
      }
    },
  },
  computed: {
    currentPage() {
      return this.currentPageNumber;
    },
    totalPages() {
      const pagination = this.paginationInfo[this.currentActiveSheetIndex];
      return pagination ? pagination.totalPages : 1;
    },
    showPaginationControls() {
      const pagination = this.paginationInfo[this.currentActiveSheetIndex];
      return pagination && pagination.totalPages > 1;
    }
  },
  watch: {
    height(newHeight) {
      if (this.$refs.univerContainer) {
        this.$refs.univerContainer.style.height = `${newHeight}px`
      }
  // },
  //   'scriptViewState.columnPageNow': {
  //     handler(newPage, oldPage) {
  //       if (newPage !== oldPage && this.script && this.script.resultList && this.script.resultList.length > 0) {
  //         this.refreshCurrentSheetData();
  //       }
  //     },
  //     deep: true
    }
  }
}
</script>

<style scoped>
.univer-container {
  width: 100%;
  height: 100%;
  border: 1px solid #dcdee2;
  position: relative;
}

.univer-sheet {
  width: 100%;
  height: calc(100% - 50px);
}

.pagination-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  border-top: 1px solid #dcdee2;
  height: 50px;
}

.page-info {
  margin: 0 15px;
  font-size: 14px;
}

.page-size-info {
  margin-left: 15px;
  font-size: 12px;
  color: #888;
}
</style>
