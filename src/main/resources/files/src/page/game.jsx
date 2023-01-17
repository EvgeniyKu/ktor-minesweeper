import Board from "../components/board";
import { Layout, Row, Col, Typography, Button } from "antd";
import { useSelector } from "react-redux";
import Timer from "../components/timer";
import { useNavigate } from "react-router-dom";
import socket from "../socket";
import { START_GAME } from "../api/actionsSocket";

const { Content, Sider } = Layout;
const { Text } = Typography;
export default function Game() {
	const navigate = useNavigate();
	const score = useSelector(({ stateGame }) => stateGame.score);
	const gameState = useSelector(({ stateGame }) => stateGame.gameState);
	const handleGoHome = () => {
		navigate("/");
	};
	const onNewGame = () => {
		socket.send(JSON.stringify(START_GAME("easy")));
	};
	return (
		<div className="wrapper">
			<div id="cursor_users"></div>
			<Layout>
				<Content className="content-board">
					<Board />
				</Content>
				<Sider theme="light" className="content-sider">
					<Content>
						<Row>
							<Col>
								<Text style={{ fontSize: "20px" }}>Count flag: {score}</Text>
							</Col>
						</Row>

						<Row>
							<Col>
								<Text style={{ fontSize: "20px" }}>
									Timer: <Timer gameState={gameState} />
								</Text>
							</Col>
						</Row>
						<hr></hr>
						<Row>
							<Col>
								<Text style={{ fontSize: "20px" }}>
									<Button onClick={onNewGame} type="primary" ghost>
										New game
									</Button>
								</Text>
							</Col>
							<Col>
								<Text style={{ fontSize: "20px" }}>
									<Button onClick={handleGoHome} type="primary" ghost>
										home
									</Button>
								</Text>
							</Col>
						</Row>
						<hr></hr>
					</Content>
				</Sider>
			</Layout>
		</div>
	);
}
