import { reactive, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { isPlainObject } from 'lodash-es';
import { createGlobalState } from '@vueuse/core';
import { FMessage, FModal } from '@fesjs/fes-design';

function useLetgoGlobal() {
  const router = useRouter();
  const route = useRoute();

  const $context = reactive({});
  const letgoContext = $context;

  $context.userInfo = {
    user: '',
    uriList: [],
    roleList: [],
  };
  $context.publicPath = './';
  $context.urlParams = computed(() => route.query || {});
  $context.navigateTo = (routeName, params) => {
    if (params && isPlainObject(params)) {
      router.push({
        name: routeName,
        query: params,
      });
    } else {
      router.push({
        name: routeName,
      });
    }
  };
  $context.navigateBack = (routeName, params) => {
    router.back();
  };

  const $utils = {
    FMessage,
    FModal,
  };
  const utils = $utils;

  const __globalCtx = {
    $context,
    letgoContext,
    $utils,
    utils,
  };

  return new Proxy(__globalCtx, {
    get(obj, prop) {
      if ([].includes(prop) && !__globalCtx[prop].hasBeenCalled)
        __globalCtx[prop].trigger();

      return obj[prop];
    },
  });
}

export const useSharedLetgoGlobal = createGlobalState(useLetgoGlobal);
