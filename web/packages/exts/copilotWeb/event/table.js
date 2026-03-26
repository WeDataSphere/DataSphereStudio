import api from '@dataspherestudio/shared/common/service/api';
import util from '@dataspherestudio/shared/common/util';
import Vue from 'vue';
import plugin from '@dataspherestudio/shared/common/util/plugin'
export default class EventTableFn {
  constructor({ dispatch }) {
    this.dispatch = dispatch;
  }
  queryTable(tableName) {
    const code = `select * from ${tableName} limit 100`;
    const filename = `${tableName}_select.hql`;
    const md5Path = util.md5(filename);
    this.dispatch('Workbench:add', {
      id: md5Path,
      filename,
      filepath: '',
      // saveAs表示临时脚本，需要关闭或保存时另存
      saveAs: true,
      noLoadCache: true,
      code,
    }, (f) => {
      if (!f) {
        return;
      }
      Vue.nextTick(() => {
        this.dispatch('Workbench:run', { id: md5Path });
      })
    });
  }
  describeTable({ dbName, fileName, filenamePath }) {
    const md5 = util.md5(filenamePath);
    const ext = plugin.emitHook('script_dbtb_details', {
      context: this,
      params: {
        type: 'tableDetails',
        filename: filenamePath,
        md5
      }
    })
    if (ext) {
      return
    }
    const waitFor = [];
    const params = {
      database: dbName,
      tableName: fileName,
    };
    waitFor.push(this.getTableBaseInfo(params));
    if (waitFor.length) {
      Promise.all(waitFor).then(([tableBaseInfo]) => {
        this.dispatch('Workbench:add', {
          id: md5,
          filename: filenamePath,
          filepath: '',
          data: {
            dbName,
            name: fileName,
            baseInfo: tableBaseInfo
          },
          type: 'tableDetails',
          currentNodeKey: ''
        }, () => {
        });
      }).catch(() => {
      });
    }
  }
  getTableBaseInfo(params) {
    return new Promise((resolve) => {
      api.fetch('/datasource/getTableBaseInfo', params, 'get').then((rst) => {
        resolve(rst.tableBaseInfo);
      });
    });
  }
}