/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import hql from './languages/hql';
import log from './languages/log';
import sh from './languages/sh';
import out from './languages/out';
import defaultView from './theme/defaultView';
import logview from './theme/logView';
import hqlKeyword from './keyword/hql';
import pythonKeyword from './keyword/python';
import shKeyword from './keyword/sh';

import * as monaco from 'monaco-editor';
import { sendLLMRequest } from '@dataspherestudio/shared/common/helper/aicompletion';

const languagesList = monaco.languages.getLanguages();
const findLang = find(languagesList, (lang) => {
  return lang.id === 'hql';
});
const uselsp = localStorage.getItem('scriptis-edditor-type') === 'lsp'
if (!findLang && !uselsp) {
  // 注册languages
  hql.register(monaco);
  log.register(monaco);
  sh.register(monaco);
  out.register(monaco);
  // 注册theme
  defaultView.register(monaco);
  logview.register(monaco);

  // 注册关键字联想
  hqlKeyword.register(monaco);
  pythonKeyword.register(monaco);
  shKeyword.register(monaco);
  monaco.languages.registerInlineCompletionsProvider("hql", {
    provideInlineCompletions: async function (model, position, context, token) {
      let language = window.__scirpt_language || "sql";
      delete window.__scirpt_language;
      if (window.$APP_CONF && window.$APP_CONF.aisuggestion !== true ) {
        return {
          items: []
        }
      }
      const value = model.getValue();
      // 使用getOffsetAt()获取光标位置的绝对偏移量
      const cursorOffset = model.getOffsetAt(position);
      // 根据绝对偏移量分割内容
      const prefix = value.substring(0, cursorOffset);
      const suffix = value.substring(cursorOffset);
      return sendLLMRequest({
        language,
        segments: {
          prefix,
          suffix,
        }
      }, position)
    },
    handleItemDidShow() {
      // window.inlineCompletions = []
      // console.log('handleItemDidShow')
    },
    freeInlineCompletions(arg) {
      // console.log(arg, 'freeInlineCompletions')
    }
  });
}

export default monaco;
