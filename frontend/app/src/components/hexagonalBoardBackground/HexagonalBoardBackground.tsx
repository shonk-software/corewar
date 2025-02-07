import { MutableRefObject, useEffect, useRef } from "react";
import { HexagonalTileProps } from "@/components/hexagonalTile/HexagonalTile.tsx";
import "./HexagonalBoardBackground.css";
import CanvasVisu from "@/components/canvasVisu/CanvasVisu.tsx";
import { defaultTileProps } from "@/lib/DefaultTileProps.ts";

// This is just our "funny background" component. I think there is no real benefit in writing tests for this and
// if we would want to write tests, I feel it would be very difficult. This is mostly visual design stuff.
function HexagonalBoardBackground() {
	function turnOffCells(cells: MutableRefObject<number[]>) {
		if (!boardRef.current) {
			console.error("Board ref is undefined!");
			return;
		}

		boardRef.current.drawChanges(
			cells.current.map((value) => {
				return {
					hexIndex: value,
					newProps: {
						...defaultTileProps,
						fill: "#000000",
						isDimmed: false,
						stroke: "gray",
						strokeWidth: "16",
						textContent: "",
					},
				};
			}),
		);

		cells.current = [];
	}

	function turnOnRandomCellToColor(
		listOfActiveCells: MutableRefObject<number[]>,
		color: string,
	) {
		if (!boardRef.current) {
			console.error("Board ref is undefined!");
			return;
		}

		let randomIndex: number;

		do {
			randomIndex = Math.floor(Math.random() * HEX_COUNT);
		} while (listOfActiveCells.current.includes(HEX_COUNT));

		boardRef.current.drawChanges([
			{
				hexIndex: randomIndex,
				newProps: {
					...defaultTileProps,
					fill: color,
					isDimmed: false,
					stroke: color,
					strokeWidth: "16",
					textContent: "",
				},
			},
		]);
		listOfActiveCells.current.push(randomIndex);
	}

	const HEX_COUNT: number = 80;

	const BACKGROUND_BLINKING_INTERVAL_MS: number = 1200;
	const RED_CELL_COUNT: number = 15;
	const BLUE_CELL_COUNT: number = 15;

	const activeCells = useRef<number[]>([]);

	useEffect(() => {
		const interval = setInterval(() => {
			if (!boardRef.current) {
				return;
			}

			turnOffCells(activeCells);

			for (let i: number = 0; i < RED_CELL_COUNT; i++) {
				turnOnRandomCellToColor(activeCells, "#FF006E");
			}

			for (let i: number = 0; i < BLUE_CELL_COUNT; i++) {
				turnOnRandomCellToColor(activeCells, "#00FFFF");
			}
		}, BACKGROUND_BLINKING_INTERVAL_MS);

		// Clear interval on component unmount
		return () => clearInterval(interval);
	}, []);

	const boardRef = useRef<{
		drawChanges: (
			updates: {
				hexIndex: number;
				newProps: HexagonalTileProps;
			}[],
		) => void;
	}>(null);

	return (
		<div className={"background-animation"}>
			<CanvasVisu
				defaultTileProps={{
					...defaultTileProps,
					strokeWidth: "16",
				}}
				canvasWidth={3400}
				canvasHeight={2600}
				hex_count={HEX_COUNT}
				scale_factor_for_space_between_hexes={0.91}
				cornerRadius={32}
				ref={boardRef}
			/>
		</div>
	);
}

export default HexagonalBoardBackground;
