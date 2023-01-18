export interface SocketI {
	send(message: string): void;
	close(): void;
	addEventListener(event: string, fn: any): void;
	removeEventListener(event: string, fn: any): void;
	set onerror(f: callbackFn);
	set onmessage(f: callbackFn);
	set onclose(f: callbackFn);
	get readyState(): number;
}

type callbackFn = (arg: any) => void;

export class Socket implements SocketI {
	socket: WebSocket;
	constructor(url: string) {
		this.socket = new WebSocket(url);
	}
	send(message: string): void {
		if (this.readyState === 0) {
			setTimeout(() => this.send(message), 1000);
		} else if (this.readyState === 1) {
			this.socket.send(message);
		}
	}
	addEventListener(event: string, fn: any) {
		this.socket.addEventListener(event, fn);
	}
	removeEventListener(event: string, fn: any) {
		this.socket.removeEventListener(event, fn);
	}
	set onopen(f: callbackFn) {
		this.socket.onopen = f;
	}
	set onerror(f: callbackFn) {
		this.socket.onerror = f;
	}
	set onmessage(f: callbackFn) {
		this.socket.onmessage = f;
	}
	set onclose(f: callbackFn) {
		this.socket.onclose = f;
	}
	close() {
		this.socket.close();
	}
	get readyState(): number {
		return this.socket.readyState;
	}
}
