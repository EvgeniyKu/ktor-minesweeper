import { START_GAME } from "../../api/actionsSocket";
import { useNavigate } from "react-router-dom";
import socket from "../../socket";
export default function Ui(props) {
	const navigate = useNavigate();
	return (
		<div className="panel">
			<div className="panel__left">
				Мной добавленно флагов:
				<span id="count-flag">0</span>
				Количество всех флагов
				<span id="count-flag-all">0</span>
			</div>
			<div className="panel__center">
				<div id="panel__new-game" className="panel__new-game">
					<button
						data-difficulty="easy"
						onClick={() => {
							socket.send(JSON.stringify(START_GAME("easy")));
							navigate("/game");
						}}
					>
						Easy
					</button>
					<button data-difficulty="medium">Medium</button>
					<button data-difficulty="hard">Hard</button>
				</div>
				<div className="panel__custom-name-game">
					<label htmlFor="rows">Rows: </label>
					<input type="number" id="rows" min="5" />
					<label htmlFor="columns">Col: </label>
					<input type="number" id="columns" min="5" />
					<label htmlFor="bombs">bombs: </label>
					<input type="number" id="bombs" min="1" />
					<button>New custom game</button>
				</div>
			</div>
			<div className="panel__right">
				Количество секунд: <span id="timer">0</span>
			</div>
		</div>
	);
}
