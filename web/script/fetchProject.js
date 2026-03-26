let path = require("path");
let fs = require("fs");
let child_process = require('child_process');

let showExeResultVal = false;
let log;

// 控制是否打印删除日志
if (!showExeResultVal) {
  log = function () { }  //不打印
} else {
  log = console.log; //打印
}

/**
  * @function createDirsSync
  * @description 递归创建路径层次中不存在的目录
  */
function createDirsSync(dir) {
  // eslint-disable-next-line require-jsdoc
  function mkDirs(url) {
    if (fs.existsSync(url)) {
      i = i + 1;
      if (len > i) {
        url = url + "/" + dirs[i];
        mkDirs(url);
      }
    } else {
      mkDir(url)
    }
  }
  // 创建文件
  // eslint-disable-next-line require-jsdoc
  function mkDir(url) {
    fs.mkdirSync(url);
    i = i + 1;
    if (len > i) {
      url = url + "/" + dirs[i];
      mkDir(url);
    }
  }
  if (dir == "." || dir == "..") {
    return;
  }
  var dirs = dir.split('/');
  if (dirs[0] == '.' || dirs[0] == "..") {
    dirs[1] = dirs[0] + "/" + dirs[1];
    dirs.shift();
  }
  if (dirs[dirs.length - 1] == "") {
    dirs.pop();
  }
  if (dirs[0] == "") dirs[0] = "/"; //兼容mac等以/开头的绝对路径
  var len = dirs.length;
  var i = 0;
  var url = dirs[i];
  // 启动递归函数
  // 逐级检测有没有当前文件夹，没有就创建，有就继续检测下一级
  mkDirs(url);
}

/**
  * @function 同步方式，递归删除指定目录下的所有文件/文件夹
  * @param {Object} paramsObj 完整的参数对象信息
  * @param {String} paramsObj.fileUrl 要删除的文件/文件夹路径
  * @param {Boolean} paramsObj.flag 是否删除最外层目录，不传或为false表示不删除，true表示删除.
  * @param {string[]} paramsObj.delExactType  当删除的是一个非空文件夹时，删除后代文件中指定的某种类型文件
*/
function deleteFile(paramsObj) {
  let { fileUrl, flag, delExactType } = paramsObj;
  return delFile({ fileUrl, flag, delExactType });
}

/**
  * @function 同步方式，删除指定目录下的所有文件/文件夹
  * @param {String} fileUrl 要删除的文件/文件夹路径
  * @param {Boolean} flag 是否删除最外层目录，不传或为false表示不删除，true表示删除.
  * @return {Boolean} true/false 表示操作是否成功
  * @example
  *  let res=delFile("./hello", true, true);
  *  console.log("res",res)
  */
function delFile({ fileUrl, flag, delExactType }) {
  var i;
  // 控制递归结束后，在最外层时进行返回
  if (i == undefined) i = 0;
  else i++;

  // 文件不存在时直接结束
  if (!fs.existsSync(fileUrl)) {
    log("你要删除的 " + fileUrl + " 不存在!");
    return false;
  }
  // 当前删除对象为文件夹时
  if (fs.statSync(fileUrl).isDirectory()) {
    var files = fs.readdirSync(fileUrl);
    var len = files.length,
      removeNumber = 0;
    if (len > 0) {
      files.forEach(function (file) {
        var url = fileUrl + '/' + file;
        if (fs.statSync(url).isDirectory()) {
          delFile({ fileUrl: url, flag: true, delExactType }); //对于文件夹递归调用自身,由于这里固定传入了true,所以子文件夹一定会被删除
        } else {
          let extname = path.extname(file);//获取文件的后缀名
          if (delExactType && !delExactType.includes(extname)) return; //如果指定了要删除的具体名字文件类型，那么没在指定中的内容就不进行删除
          fs.unlinkSync(url); //对于文件直接进行删除
          log('删除文件' + url + '成功');
        }
        removeNumber++;
      });
      // 是否删除自身
      if (len == removeNumber && flag) {
        fs.rmdirSync(fileUrl);
        log('删除文件夹' + fileUrl + '成功');

      }
    } else if (len == 0 && flag) {
      // 对于最外层目录，将根据调用delFile时的第二个参数是否为true决定
      fs.rmdirSync(fileUrl);
      log('删除文件夹' + fileUrl + '成功');

    }
  } else {
    // 当前删除对象为文件时
    fs.unlinkSync(fileUrl);
    log('删除文件' + fileUrl + '成功');
  }
  if (i == 0) {
    return true;//表示删除完成
  }
}

/**
  * @function copycutFiledir
  * @param {Object} paramsObj 同步方式，对文件/文件夹执行复制/剪切操作
  * @param {String} paramsObj.inputFileUrl 要复制/剪切的文件/文件夹路径
  * @param {String} paramsObj.outFileUrl 要将文件/文件夹要复制/剪切到哪里(复制目录只能写目录路径，复制文件只能写文件路径)
  * @param {String} paramsObj.copyOrCut  复制还是剪切,值为copy|cut ，默认为复制copy
  * @param {Boolean} paramsObj.showExeResult  是否显示写入操作完后的提示，默认为true：显示。
  * @param {Boolean} paramsObj.rewrite  对于已经存在的文件是否跳过，false跳过, 当值为true时表示进行覆盖
*/
function copycutFiledir(paramsObj) {
  try {
    let {
      inputFileUrl, //要复制或剪切的文件路径
      outFileUrl, //要把文件或路径复制剪切到哪里
      copyOrCut = "copy",//复制(copy) or 剪切(cut),默认复制
      showExeResult = true,
    } = paramsObj;

    // 标识时文件还是文件夹
    let fileOrDir;//dir|file

    // 控制是否打印日志
    if (showExeResult == false) {
      log = function () { }
    }

    if (inputFileUrl === outFileUrl) {
      log("【 输入输出文件不能相同】");
      return;
    }

    // 先判断文件/文件夹是否存在
    if (!fs.existsSync(inputFileUrl)) {
      log("【 " + inputFileUrl + " 】不存在!");
      return false;
    }

    // 先判断是文件还是文件夹
    if (fs.statSync(inputFileUrl).isDirectory()) {
      fileOrDir = "dir";
    }
    else {
      fileOrDir = "file";
    }
    let res;
    if (fileOrDir == "file") {
      res = handleFile(paramsObj);// 如果是文件，开始操作
    } else {
      handleDir(paramsObj);  // 如果是文件夹，开始操作
    }
    // 如果是剪切操作，还需要删除下原始文件
    if (copyOrCut == "cut" && res != "跳过") deleteFile({ fileUrl: inputFileUrl, showExeResult, flag: true });
    // 操作完成后的提示
    log("【 " + inputFileUrl + " 】成功 " + copyOrCut + " 到 【" + outFileUrl + " 】");
  }
  catch (err) {
    console.log("=========err=========", err);
  }
}

/**
 * 以同步方式读取指定文件的内容
 * @param {*} paramsObj
 */
function readFileContent(paramsObj) {
  let { filePath, readEncode, returnType = "string" } = paramsObj;
  let buffer;
  if (readEncode) {
    buffer = fs.readFileSync(filePath, readEncode);
  } else {
    buffer = fs.readFileSync(filePath);
  }
  if (returnType == "string") return String(buffer);
  else return buffer;
}

const excludeFiles = [
  '\\web\\packages\\dss\\.env',
  '\\web\\packages\\dss\\package.json'
]

/**
 * 对文件实现复制或剪切的操作
 **/
function handleFile(params) {
  let {
    inputFileUrl, //要复制或剪切的文件路径
    outFileUrl, //要把文件或路径复制剪切到哪里
    rewrite = true,//如果目标文件夹已经存在此文件，是否要覆盖，默认覆盖
    showExeResult
  } = params;
  // 如果为复制操作（先读取，在写入）
  const skip = excludeFiles.some(it => inputFileUrl.indexOf(it) > -1)
  if (skip) {
    console.log(inputFileUrl)
    return "跳过"
  }
  let content = readFileContent({ filePath: inputFileUrl, returnType: "buffer" });
  // 判断目标目录是否已经存在此文件
  let state = fs.existsSync(outFileUrl);//为true表示存在
  if (!state) {
    writeFile({ path: outFileUrl, content, showExeResult });
  } else {
    if (state && rewrite) {
      writeFile({ path: outFileUrl, content, showExeResult });
      log("【 " + inputFileUrl + "】 已经存在，自动覆盖此文件");
    }
    // 不覆盖已经存在的文件
    if (state && !rewrite) {
      log("【 " + inputFileUrl + "】 已经存在，自动跳过此文件");
      return "跳过";
    }
  }
}

/**
 * 对文件夹进行写入
 * @param {*} params
 */
function handleDir(params) {
  let {
    inputFileUrl, //要复制或剪切的文件路径
    outFileUrl, //要把文件或路径复制剪切到哪里
  } = params;

  // 开始递归进行复制
  var files = fs.readdirSync(inputFileUrl);
  var len = files.length;
  if (len > 0) {
    files.forEach(function (file) {
      var inpUrl = path.join(inputFileUrl,file);
      var outUrl = path.join(outFileUrl,file);
      // 操作目录
      if (fs.statSync(inpUrl).isDirectory()) {
        var n = fs.readdirSync(inputFileUrl);
        if (n.length > 0) {
          handleDir({ ...params, inputFileUrl: inpUrl, outFileUrl: outUrl });
        } else {
          fs.rmdirSync(outFileUrl);  //创建空文件夹
        }
      } else {
        handleFile({ ...params, inputFileUrl: inpUrl, outFileUrl: outUrl });
      }
    });
  } else {
    log("【 " + inputFileUrl + "】 是空目录，没有可复制文件/文件夹");
    return;
  }
}

/**
 * 处理解析绝对路径（windows）
 * @param {*} thePath
 * @param {*} content
 * @param {*} showExeResult
 */
function handleAbsolutePath(thePath, content, showExeResult) {
  // if (/^[A-Za-z]:/.test(thePath)) { } // 绝对路径判断规则
  let start = thePath[0] + thePath[1] + thePath[2]; //取出盘符
  let other = thePath.replace(start, ""); //去掉盘符，只剩下路径
  let pathArr = [thePath[0] + thePath[1]];
  let otherArr = other.split("\\"); //将路径转换为数组
  otherArr.splice(otherArr.length - 1, 1);// 移除文件名，只剩下纯路径
  pathArr.push(...otherArr);
  // 开始递归实现对不存在的目录进行创建
  let url = "";
  for (let i = 0; i < pathArr.length; i++) {
    // 路径不存在就创建
    url += pathArr[i] + "\\";
    if (!fs.existsSync(url)) {
      fs.mkdirSync(url);
    }
  }
  // 创建文件
  fs.writeFileSync(thePath, content);
  if (showExeResult) {
    console.log(thePath + "创建成功");
  }
}

/**
  * @description 同步方式，向一个文件写入内容，不存在就创建，存在就覆盖
  * @param {Object} paramsObj 完整的参数对象信息
  * @param {String} paramsObj.path 要写入的文件路径,可绝对路径，可相对路径
  * @param {Any} paramsObj.content 要写入的文件内容
  * @param {Boolean} paramsObj.showExeResult  是否显示文件操作完后的提示，默认为true：显示。
  * @return  {Boolean} true/false 表示写入成功与否的状态
  */
function writeFile(paramsObj) {

  let { path, content, showExeResult } = paramsObj;
  if (showExeResult == undefined) showExeResult = true; //默认显示提示
  // 绝对路径判断规则
  if (/^[A-Za-z]:/.test(path)) {
    try {
      handleAbsolutePath(path, content, showExeResult);
      return true;
    } catch (err) {
      console.log("======创建文件错误====", err)
      return false;
    }
  }
  return writePathFile({ path, content, showExeResult });
}

/**
 * 写入文件的外层调用函数
 * @param {*} param0
 */
function writePathFile({ path, content, showExeResult }) {
  var pathA = path.split("/");
  pathA.pop();
  // 生成提示语
  let msg = "";
  try {
    // 递归创建不存在的目录
    createDirsSync(pathA.join("/"));
    // 写入文件
    fs.writeFileSync(path, content);
    msg = path + " 创建成功."
    // 控制默认的日志打印
    if (showExeResult) {
      console.log(msg);
    }
    return true;

  } catch (err) {
    msg = path + " 创建失败.";
    console.log(msg, err);
    return false;
  }
}


/**
 * @function checkoutDir
 * @description 从远程git仓库拉取指定目录内容
 * @param {object} param 参数对象
 * @param {string} param.gitUrl  欲拉取项目的git仓库地址
 * @param {string} param.dirName 拉取的指定目录名
 * @param {string} param.saveDir 拉取的内容保存地址，默认为当前目录
 * @param {string} param.delGit  拉取完后是否删除.git文件（默认为true,删除）
 * @param {string} param.isUseEndDir  如果指定的目录有多个层级，那么是否取最后一级，默认为true,取最后一级.否则取完整路径
 * @example
 *
 *  param.gitUrl  欲拉取项目的git仓库地址
 *  param.dirName 拉取的指定目录名
 *  param.saveDir 拉取的内容保存地址，默认为当前目录
 *  param.delGit  拉取完后是否删除.git文件（默认为true,删除）
 *  param.isUseEndDir  如果指定的目录有多个层级，那么是否取最后一级，默认为true,取最后一级.否则取完整路径
 *
 * checkoutDir({
 *     gitUrl: '',
 *     dirName: '',
 *     saveDir: "./aa
 * })
 */
function checkoutDir(param) {
  let {
    gitUrl,
    dirName,
    branchName = child_process.execSync('git branch --show-current'),
    saveDir = path.resolve("."),
    delGit = true,
    isUseEndDir = true,
  } = param;

  if (!gitUrl) {
    console.error("必须指定欲拉取项目的git仓库地址，gitUrl 参数必填！"); return;
  }
  if (!dirName) {
    console.error("必须指定欲拉取项目的目录名，dirName 参数必填！"); return;
  }
  createDirsSync(saveDir)
  // 拉取下来的代码盛放的临时文件夹各目录
  let randDirName = "tmp_folder_" + new Date().getTime();//获取时间戳作为随机目录名
  let dir_randDirName = path.join(saveDir, randDirName);
  let dir_randDirName_dir = path.join(dir_randDirName, dirName);
  let dir_randDirName_git = path.join(dir_randDirName, "/.git");
  // 需要保存到的位置目录(如果目录有多个层级，那么默认取最后一级)
  if (isUseEndDir) {
    let dirArr = dirName.split("/");
    dirArr = dirArr.filter(ele => ele != "");
    if (dirArr.length > 1) {
      dirName = dirArr[dirArr.length - 1];
    }
  }
  let saveDir_dirName = path.join(saveDir, dirName);
  let saveDir_git = path.join(saveDir, ".git");

  console.log(dirName + "项目内容获取中，请稍等......");

  const os = require('os');
  let sysType = os.type();
  let cli = "";
  if (sysType === "Windows_NT") {  //windows
    cli = `git init & git remote add origin ${gitUrl} & git config core.sparsecheckout true & echo ${dirName} >> .git/info/sparse-checkout & git pull origin ${branchName}`;
  } else { //mac 和 linux
    cli = `
      git init
      git remote add origin ${gitUrl}
      git config core.sparsecheckout true
      echo ${dirName} >> .git/info/sparse-checkout
      git pull origin ${branchName}`;
  }
  //开始克隆远程项目
  child_process.exec('mkdir ' + dir_randDirName, function (error) {
    if (error) console.log(error)
    child_process.exec(cli, { cwd: dir_randDirName }, //在dir_randDirName目录下执行git拉取操作
      function (error) {
        if (error !== null) {
          console.error('\n[项目拉取失败],请检查你的命令是否正确\n\n', error);
        } else {
          console.log("【 " + dirName + "目录获取成功 】");
          // 如果指定了要删除.git文件夹,那就直接删除
          if (delGit) {
            deleteFile({ fileUrl: dir_randDirName_git, flag: true, showExeResult: false });
          } else {
            copycutFiledir({
              inputFileUrl: dir_randDirName_git,
              outFileUrl: saveDir_git,
              copyOrCut: "copy",
              showExeResult: false
            });
          }
          // 将实际内容移到外层(直接将里面的全部复制出来)
          copycutFiledir({
            inputFileUrl: dir_randDirName_dir,
            outFileUrl: saveDir_dirName,
            copyOrCut: "copy",
            showExeResult: false
          });
          // 拉取成功后，执行删除临时文件夹
          deleteFile({ fileUrl: dir_randDirName, flag: true, showExeResult: false });
        }
      });
  });
}

const pobj = {}
const params = process.argv[2].split('##')

params.forEach(item=> {
  const kv = item.split('=')
  pobj[kv[0]] = kv[1]
})

checkoutDir(pobj)
