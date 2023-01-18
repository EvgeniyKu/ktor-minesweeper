import { useNavigate } from "react-router-dom";
import style from "./style.module.css";
import { Row, Col, Button } from "antd";
export default function createGame(props) {
	const navigate = useNavigate();
	return (
		<Row className={style["indent"]} justify="space-around">
			<Col>
				<Button onClick={() => navigate("/game")} type="primary" ghost>
					Easy
				</Button>
			</Col>
			<Col>
				<Button type="primary" ghost>
					Medium
				</Button>
			</Col>
			<Col>
				<Button type="primary" ghost>
					Hard
				</Button>
			</Col>
		</Row>
	);
}
