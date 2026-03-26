import { request } from '@dataspherestudio/shared';

// 获取节点分类列表
export function getNodeGroupList(params = {}) {
  return request
    .fetch('dss/framework/appconnmanager/getnotegroup', params, 'get')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data.nodeGroup;
    })
    .catch((error: any) => {
      //
    });
}
// 获取所有Appcoon 下拉
export function getAllAppconnNameList(params = {}) {
  return request
    .fetch('dss/framework/project/appconn/getAppConnsName', params, 'get')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data.appConnsName;
    })
    .catch((error: any) => {
      //
    });
}

// 查看节点信息
export function getNodeInfo(params = {}) {
  return request
    .fetch('dss/framework/appconnmanager/getnode', params, 'get')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data;
    })
    .catch((error: any) => {
      //
    });
}
// 保存节点信息
export function saveNodeInfo(params = {}) {
  return request
    .fetch(
      'dss/framework/appconnmanager/savenode',
      JSON.stringify(params),
      'post'
    )
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data.node;
    })
    .catch((error: any) => {
      //
    });
}

// 查询属性
export function queryAttribute(params = {}) {
  return request
    .fetch('dss/framework/appconnmanager/getui', params, 'get')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data;
    })
    .catch((error: any) => {
      //
    });
}

// 查询校验规则
export function queryValidateRule(params = {}) {
  return request
    .fetch('dss/framework/appconnmanager/getuivalidate', params, 'get')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data.validateList;
    })
    .catch((error: any) => {
      //
    });
}

// 保存节点属性
export function saveNodeAttribute(params = {}) {
  return request
    .fetch('dss/framework/appconnmanager/addui', JSON.stringify(params), 'post')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data.ui;
    })
    .catch((error: any) => {
      //
    });
}

// 保存校验规则
export function saveVerificationRuleData(params = {}) {
  return request
    .fetch(
      'dss/framework/appconnmanager/savevalidate',
      JSON.stringify(params),
      'post'
    )
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data.validate;
    })
    .catch((error: any) => {
      //
    });
}

// 校验规则关联属性
export function ruleBindAttribute(params = {}) {
  return request
    .fetch('dss/framework/appconnmanager/uiaddvalidate', params, 'post')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data;
    })
    .catch((error: any) => {
      //
    });
}

// 属性取消关联规则
export function unbindVerificationRule(params = {}) {
  return request
    .fetch('dss/framework/appconnmanager/uideletevalidate', params, 'post')
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data;
    })
    .catch((error: any) => {
      //
    });
}

// 节点关联属性
export function nodeBindAttribute(params = {}) {
  return request
    .fetch(
      'dss/framework/appconnmanager/nodeaddui',
      JSON.stringify(params),
      'post'
    )
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data;
    })
    .catch((error: any) => {
      //
    });
}

// 节点删除属性
export function nodeDeleteAttribute(params = {}) {
  return request.fetch(
    'dss/framework/appconnmanager/deleteui',
    JSON.stringify(params),
    'post'
  );
}

// 节点取消关联属性
export function nodeUnbindAttribute(params = {}) {
  return request
    .fetch(
      'dss/framework/appconnmanager/nodedeleteui',
      JSON.stringify(params),
      'post'
    )
    .then((res: any) => {
      if (!res) {
        return {};
      }
      return res.data;
    })
    .catch((error: any) => {
      //
    });
}
