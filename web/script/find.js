/* eslint-disable require-jsdoc */
var fs = require('fs');
var path = require('path');
// 中文正则
var reg = /[\u4e00-\u9fa5]+[：？！:]?/g;
var json = {};

// 要处理的目录参数
var dir = process.argv[2];

var dirPath = path.resolve(dir);

json.dirPath = dirPath;
json.found = {};

// 排除的目录
var excludeDirs = ['vue-process-demo','dataGovernance','dataService','dolphinScheduler', 'cyeditor']

function findCNText(filePath) {
  fs.readdir(filePath, function (err, files) {
    if (err) {
      console.warn(err)
    } else {
      files.forEach(function (filename) {
        var filedir = path.join(filePath, filename);
        fs.stat(filedir, function (eror, stats) {
          if (eror) {
            console.warn('获取文件stats失败');
          } else {
            var isFile = stats.isFile();
            var isDir = stats.isDirectory();
            // 排除目录
            const except = excludeDirs.some(it => {
              return filedir.indexOf(it) > -1
            })
            // 仅处理vue js 文件
            if (!except && isFile && /\.vue|js$/.test(filename)) {
              var content = fs.readFileSync(filedir, 'utf-8');
              // 忽略html注释
              content = content.replace(/<!--[\w\W\r\n]*?-->/gmi, '');
              // 忽略js注释
              content = content.replace(/\/\*[^\/]*\/|\/\/.+\n?/g, '');
              // console.log console.error
              content = content.replace(/console\.(log|error|warning|info|debug)\([^()\r]*.*\);*/gmi, '');
              // 提取中文
              var arrs = content.match(reg) || [];
              var key = filedir.split('packages')[1].replaceAll('\\', '.')
              arrs.forEach(item => {
                json.found[`{{${key}}}.${item}`] = item;
              });
            }
            if (isDir && !except ) {
              findCNText(filedir);
            }
          }
        })
      });
    }
    fs.writeFile('./cn.json', JSON.stringify(json), function (err) {
      if (err) {
        throw err;
      }
    })
  });
}

findCNText(dirPath);

//  node ./script/find.js ./packages/
