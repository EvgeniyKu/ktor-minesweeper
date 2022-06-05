class View {
	constructor(element){
		this.elApp = element;
		this.elBoard = element.querySelector("#board");
	}
	set board(html){
		this.elBoard.innerHTML = html;
	}
	set tick(sec){
		this.elApp.querySelector("#timer").innerHTML = sec;
	}
	set myFlag(count){
		this.elApp.querySelector("#count-flag").innerHTML = count;
	}
	set flagAll(count){
		this.elApp.querySelector("#count-flag-all").innerHTML = count;
	}
	set endGameIsWin(isWin){
		let nameClass = isWin?"win":"lose";
		this.elBoard.classList.add(nameClass);
	}
	
	resetEndGame(){
		this.elBoard.classList.remove("lose")
		this.elBoard.classList.remove("win")
	}
	renderCursorUser(user,x,y){
		let elUser = this.elApp.querySelector(`[data-username='${user}']`);
		elUser.style.left = x+"px";
		elUser.style.top = y+"px";
	}
}
class Model {
	#sendCursor = true;
	#countFlag = 0;
	#countFlagAll = 0;
	#players = [];
	constructor(urlWS){
		this.urlWS = urlWS;
	}

	set myFlag(countFlag){
		this.#countFlag = countFlag;
	}
	get myFlag(){
		return this.#countFlag;
	}
	get countFlagAll(){
		return this.#countFlagAll;
	}
	set countFlagAll(int){
		this.#countFlagAll = int;
	}
	get countFlagAll(){
		return this.#countFlagAll;
	}
	get sendCursor(){
		return this.#sendCursor;
	}
	set sendCursor(boolean){
		this.#sendCursor=boolean;
	}
	get players (){
		return this.#players;
	}
	set players (obj){
		this.#players.push(obj)
	}
}
class Presenter {
	constructor(model,view,socket){
		this.model = model;
		this.view = view;
		this.socket = socket;
		model.presenter = this;
		view.presenter = this;
		this.init();
		this.initSocket();
	}
	initSocket(){
		let self = this;
		this.socket.onopen = function(data){
			console.log(`[openWS] - Открыто соединение. ${data}`);
		}
		this.socket.onerror = function(data){
			console.log(`[ErrorWS] - Ошибка на стороне WebSocket: ${data}`);
		}

		this.socket.onmessage = function(message){
			let data = JSON.parse(message.data);
			
			switch (data["messageType"]){
				case "tick":
				self.view.tick = data["body"]['seconds'];
				break;

				case "game_state":
				if(data["body"]["gameState"] === "lose"){
					self.view.endGameIsWin = false;
				}
				if(data["body"]["gameState"] === "win"){
					self.view.endGameIsWin = true;
				}
				if(data["body"]["gameState"] === "not_started"){
					self.view.resetEndGame();
				}
				let board = data.body.board;
				self.boardCreate(board);
				break;

				case "player_positions":
				for(let item in data["body"]["positions"]){
					let players = data["body"]["positions"][item];
					let valuePlayer = self.searchPlayers(players.player.id);
					if(valuePlayer){
						self.view.renderCursorUser(players.player.id,players.x,players.y,)
					}else{
						self.addPlayers(players);
					}
				}
				break;
			}


			if(data["messageType"] !== "tick") {
				console.log(`[MessageWS] - Пришли данные от WebSocket:`,data);
			}
		}

		this.socket.onclose = function(data){
			console.log(`[CloseWS] - Соединение закрыто: ${data}`);
		}
	}
	init(){
		this.view.elBoard.addEventListener("click",el=>{
			let target = el.target;
			if(!target.classList.value.includes("col")) return null;
			if(target.classList.value.includes("flag")) return null;
			this.socket.send(JSON.stringify({
				"action": "openCell",
				"body": {
					"row": target.dataset.row,
					"column": target.dataset.col 
				}
			}))
		})
		
		this.view.elApp.querySelector(".wrapper").addEventListener("mousemove",event=>{
			if(this.model.sendCursor){
				let offsetTopBoard = this.view.elApp.querySelector(".wrapper").offsetTop;
				let offsetLeftBoard = this.view.elApp.querySelector(".wrapper").offsetLeft;
				let {clientX, clientY} = event;

				this.socket.send(JSON.stringify({
					"action": "mousePosition",
					"body":{
						"x":clientX-offsetLeftBoard,
						"y":clientY-offsetTopBoard
					}
				}));
				this.model.sendCursor = false;
				setTimeout(()=>this.model.sendCursor=true,0);
			}
		})
		

		this.view.elApp.querySelector("#panel__new-game").addEventListener("click",el=>{
			if("difficulty" in el.target.dataset){
				this.model.myFlag = 0;
				this.view.myFlag = 0
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

		this.view.elBoard.addEventListener("contextmenu",el=>{
			el.preventDefault();
			console.log(el.target);
			let target = el.target;
			if(!target.classList.value.includes("col")) return null;
			if(target.classList.value.includes("flag")){
				target.classList.remove("flag");
				this.view.myFlag = --this.model.myFlag;
				
			}else{
				target.classList.add("flag")
				this.view.myFlag = ++this.model.myFlag;
			}
			console.log(target.dataset);
			this.socket.send(JSON.stringify({
				"action": "setFlag",
				"body": {
					"row": target.dataset.row,
					"column": target.dataset.col 
				}
			}));
		});
		this.view.elApp.querySelector(".panel__custom-name-game button").addEventListener("click",e=>{
			let inputs = this.view.elApp.querySelectorAll(".panel__custom-name-game input");
			let params = {};
			for(let input of inputs){
				params[input.id] = +input.value;
			}
			this.socket.send(JSON.stringify({
				action: "startCustomGame",
				body: params
			}
			))
		});
	}
	boardCreate(arr){
		this.model.countFlagAll = 0;
		let doc = "";
		arr.forEach(listRow=>{
			doc+=`<div class="row">`;
			listRow.forEach(cell=>{
				let isflag = cell.isFlagged?"flag":"";
				if(cell.isFlagged) this.model.countFlagAll++;

				let isOpen = cell.isOpened?"open":"";

				let countOfBombs = cell.countOfBombs;
				let textOfBombs = "";
				let classOfBombs = "";
				if(countOfBombs) textOfBombs = countOfBombs;
				if(countOfBombs===9) {classOfBombs = "bomb";textOfBombs="";}
				doc+=`<div class="col ${classOfBombs} ${isflag} ${isOpen}" data-row="${cell.row}" data-col="${cell.column}">${textOfBombs}</div>`;
			});
			doc+=`</div>`;
		})
		
		this.view.board = doc;
		this.view.flagAll = this.model.countFlagAll;
	}
	searchPlayers(id){
		for(let index in this.model.players){
			if(this.model.players[index].player.id === id) return this.model.players[index];
		}
		return null;
	}
	havePlayers(id){
		return this.searchPlayers(id)?true:false;
	}
	addPlayers(objPlayers){
		this.model.players = objPlayers;

		let {player:{id},x,y}=objPlayers;
		let divPlayer = document.createElement("div");
		divPlayer.className = "user"
		divPlayer.dataset.username = id;
		divPlayer.style.left = x;
		divPlayer.style.top = y;
		this.view.elApp.querySelector("#cursor_users").append(divPlayer);
	}
}
class Socket {
	constructor(url){
		this.socket = new WebSocket(url);
		
	}
	send(message){
		this.socket.send(message);
	}
	set onopen(f){
		this.socket.onopen=f;
	}
	set onerror (f){
		this.socket.onerror = f;
	}
	set onmessage (f){
		this.socket.onmessage = f;
	}
	set onclose (f){
		this.socket.onclose = f;
	}

}

const socket = new Socket("ws://192.168.0.12:8080/minesweeper-socket");
const view = new View(document.querySelector("#app"));
const model = new Model();
console.log(socket);
const presenter = new Presenter(model,view,socket);





// document.querySelector("[data-row='1'][data-col='3']")
