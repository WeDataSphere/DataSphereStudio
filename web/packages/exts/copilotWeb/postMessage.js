// type TnoticeCallbackInfo = {
//     chatApp: string,
//     source: string,
//     eventType: string,
//     params: {
//         data?: any,
//         tables?: [],
//         [key: string]: any
//     }
// }
// export type TnoticeCallback = (params: TnoticeCallbackInfo) => void;
export default class Broadcastchannel {
  constructor(windowFrame) {
    this.channel = windowFrame || window;
    this.origin = window.location.origin;
    this.listenerCallBackFn = () => {}
  }
  /**
   * 
   * @param {TnoticeCallbackInfo} data 
   */
  post = (data) => {
    console.warn(this, data);
    this.channel.postMessage(JSON.stringify(data), this.origin);
  }
  /**
   * 
   * @param {*} event 
   * @returns 
   */
  listenerFn = (event) => {
    try {
      if (event.origin !== this.origin) {
        console.warn('非同源post message消息');
        return;
      }
      const isStringData =typeof event.data === 'string'
      const data = isStringData ? JSON.parse(event.data || '{}') : event.data;
      if (!data.source || !data.eventType) return;
      this.listenerCallBackFn(data);
    } catch (error) {
      console.error(error);
    }
  }
  /**
   * 
   * @param {TnoticeCallback} callBackFn 
   */
  listener = (callBackFn) => {
    this.listenerCallBackFn = callBackFn
    this.channel.addEventListener('message', this.listenerFn);
  }

  removeListener = () => {
    this.channel.removeEventListener('message', this.listenerFn);
  }
}
