
class Saper {
	#countFlag = 0;
	constructor(element,row,col){

		this.element = element;
		element.addEventListener("click",el=>{
			let target = el.target;
			if(!target.classList.value.includes("col")) return null;
			if(target.classList.value.includes("flag")) return null;
			socket.send(JSON.stringify({
				"action": "openCell",
				"body": {
					"row": target.dataset.row,
					"column": target.dataset.col 
				}
			}))
		})

		document.querySelector("#panel__new-game").addEventListener("click",el=>{
			if("difficulty" in el.target.dataset){
				socket.send(JSON.stringify(
				{
					"action": "startGame",
					"body": {
						"difficulty": el.target.dataset.difficulty
					}
				}
				));
			}
		});

		element.addEventListener("contextmenu",el=>{
			el.preventDefault();
			console.log(el.target);
			let target = el.target;
			if(!target.classList.value.includes("col")) return null;
			if(target.classList.value.includes("flag")){
				target.classList.remove("flag");
				this.flag--;
				
			}else{
				target.classList.add("flag")
				this.flag++;
			}
			console.log(target.dataset);
			socket.send(JSON.stringify({
				"action": "setFlag",
				"body": {
					"row": target.dataset.row,
					"column": target.dataset.col 
				}
			}))
		})
	}
	set flag(countFlag){
		this.#countFlag = countFlag;
		document.querySelector("#count-flag").innerHTML = this.#countFlag;
	}
	get flag(){
		return this.#countFlag;
	}
	lose(){
		this.element.classList.add("lose")
	}
	win(){
		this.element.classList.add("win")
	}
	reset(){
		this.element.classList.remove("lose")
		this.element.classList.remove("win")
	}
	render(arr){
		let doc = "";
		for(let i=0; i<arr.length;i++){
			doc+=`<div class="row">`;
			for(let a=0;a<arr[i].length;a++){
				let isflag = arr[i][a].isFlagged?"flag":"";
				let isOpen = arr[i][a].isOpened?"open":"";
				let countOfBombs = arr[i][a].countOfBombs;
				let textOfBombs = "";
				let classOfBombs = "";
				if(countOfBombs) textOfBombs = countOfBombs;
				if(countOfBombs===9) {classOfBombs = "bomb";textOfBombs="";}
				
				doc+=`<div class="col ${classOfBombs} ${isflag} ${isOpen}" data-row="${arr[i][a].row}" data-col="${arr[i][a].column}">${textOfBombs}</div>`;
			}
			doc+=`</div>`;
		}

		this.element.innerHTML = doc;
	}
}


let game = new Saper(document.querySelector("#main"));
const urlWs = "ws://192.168.0.12:8080/minesweeper-socket";
const socket = new WebSocket(urlWs);

socket.onopen = function(data){
	console.log(`[openWS] - Открыто соединение. ${data}`);
}
socket.onerror = function(data){
	console.log(`[ErrorWS] - Ошибка на стороне WebSocket: ${data}`);
}

socket.onmessage = function(message){
	data = JSON.parse(message.data);
	console.log(data);
	if(data["messageType"] !== "game_state") return null;
	if(data["body"]["gameState"] === "lose"){
		game.lose();
	}
	if(data["body"]["gameState"] === "win"){
		game.win();
	}
	if(data["body"]["gameState"] === "not_started"){
		game.reset();
	}
		let board = data.body.board;
		game.render(board);
	

	console.log(`[MessageWS] - Пришли данные от WebSocket:`,data);
}

socket.onclose = function(data){
	console.log(`[CloseWS] - Соединение закрыто: ${data}`);
}


// document.querySelector("[data-row='1'][data-col='3']")
