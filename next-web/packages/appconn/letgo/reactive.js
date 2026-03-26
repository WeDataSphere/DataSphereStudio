import { computed, reactive, shallowReactive } from 'vue';

export function markShallowReactive(target, properties) {
  const state = shallowReactive(properties);
  Object.keys(properties).forEach((key) => {
    Object.defineProperty(target, key, {
      get() {
        return state[key];
      },
      set(value) {
        state[key] = value;
      },
    });
  });
  return target;
}

export function markReactive(target, properties) {
  const state = reactive(properties);
  Object.keys(properties).forEach((key) => {
    Object.defineProperty(target, key, {
      get() {
        return state[key];
      },
      set(value) {
        state[key] = value;
      },
    });
  });
  return target;
}

export function markComputed(target, properties) {
  const prototype = Object.getPrototypeOf(target);
  properties.forEach((key) => {
    const descriptor = Object.getOwnPropertyDescriptor(prototype, key);
    if (descriptor?.get) {
      const tmp = computed(descriptor.get.bind(target));
      Object.defineProperty(target, key, {
        get() {
          return tmp.value;
        },
      });
    }
  });
  return target;
}
