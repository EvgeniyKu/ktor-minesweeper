export default function Cell(props) {
	const { idRow, idColumn, isOpened, isFlagged, countOfBombs } = props;
	const classCell = ["cell"];
	isOpened && classCell.push("open");
	isFlagged && classCell.push("flag");
	countOfBombs === 9 && classCell.push("bomb");
	return (
		<div
			id="cell"
			className={classCell.join(" ")}
			data-row={idRow}
			data-column={idColumn}
		>
			{countOfBombs && countOfBombs !== 9 ? countOfBombs : ""}
		</div>
	);
}
