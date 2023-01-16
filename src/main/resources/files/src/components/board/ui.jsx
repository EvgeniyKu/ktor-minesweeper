import { useMemo } from "react";
import Cell from "../cell";
import { Row, Col } from "antd";
export default function Ui(props) {
	const { board, ref, gameState } = props;
	const cells = useMemo(() => {
		if (!Array.isArray(board)) {
			return "";
		}
		return board.map((row, indexRow) => {
			const columns = row.map((dataCell, indexColumn) => {
				const {
					row: rowIndexFromAPI,
					column: columnIndexFromAPI,
					isOpened,
					isFlagged,
					countOfBombs,
				} = dataCell;
				return (
					<Col key={indexColumn}>
						<Cell
							idRow={rowIndexFromAPI}
							idColumn={columnIndexFromAPI}
							isOpened={isOpened}
							isFlagged={isFlagged}
							countOfBombs={countOfBombs}
						/>
					</Col>
				);
			});
			return <Row key={indexRow}>{columns}</Row>;
		});
	}, [board]);
	const classForBoard = ["board"];
	switch (gameState) {
		case "lose":
			classForBoard.push("lose");
			break;
		case "win":
			classForBoard.push("win");
			break;
	}
	return (
		<div ref={ref} className={classForBoard.join(" ")}>
			{cells}
		</div>
	);
}
