import { useNavigate } from "react-router-dom";
import { Row, Col, Button } from "antd";

import style from "./style.module.css";

export default function createGame() {
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
