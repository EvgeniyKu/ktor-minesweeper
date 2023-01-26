import { Row, Col, Input, Button } from "antd";

import style from "./style.module.css";

export default function CreateGameCustom() {
	return (
		<div className={style["create-game-custom"]}>
			<Row className={style.indent} wrap={false} gutter={[10, 10]}>
				<Col>
					<Input addonBefore="Rows" defaultValue={5} type="number" min={5} />
				</Col>
				<Col>
					<Input addonBefore="Col" defaultValue={5} type="number" min={5} />
				</Col>
				<Col>
					<Input addonBefore="Bombs" defaultValue={5} type="number" min={5} />
				</Col>
			</Row>
			<Row className={style.indent} justify="center">
				<Col>
					<Button style={{ width: "100%" }} type="default">
						New custom game
					</Button>
				</Col>
			</Row>
		</div>
	);
}
