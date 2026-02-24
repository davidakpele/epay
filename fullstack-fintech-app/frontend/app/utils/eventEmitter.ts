type EventCallback = (...args: any[]) => void;

class EventEmitter {
  private events: { [key: string]: EventCallback[] } = {};

  // Static properties for event names
  static readonly refreshBalance = 'refreshBalance';
  static readonly refreshNotifications = 'refreshNotifications';

  on(event: string, callback: EventCallback) {
    if (!this.events[event]) {
      this.events[event] = [];
    }
    this.events[event].push(callback);
  }

  off(event: string, callback: EventCallback) {
    if (!this.events[event]) return;
    this.events[event] = this.events[event].filter(cb => cb !== callback);
  }

  emit(event: string, ...args: any[]) {
    if (!this.events[event]) return;
    this.events[event].forEach(callback => callback(...args));
  }
}

export const eventEmitter = new EventEmitter();