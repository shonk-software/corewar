import {
	ForwardedRef,
	forwardRef,
	useEffect,
	useImperativeHandle,
	useRef,
} from "react";
import "./CanvasVisu.css";
import { CanvasVisuProps } from "@/components/canvasVisu/CanvasVisuProps.ts";
import { calcIdealColumnsAndRows, fitHexagonsToCanvas } from "@/lib/utils.ts";
import { HexagonalTileProps } from "@/domain/HexagonalTileProps.tsx";

const CanvasVisu = forwardRef(function CanvasVisu(
	{
		defaultTileProps,
		canvasWidth = 1500,
		canvasHeight = 800,
		hex_count = 8192,
		scale_factor_for_space_between_hexes = 0.9,
		cornerRadius = 0,
	}: Readonly<CanvasVisuProps>,
	ref?: ForwardedRef<{
		drawChanges: (
			updates: {
				hexIndex: number;
				newProps: HexagonalTileProps;
			}[],
		) => void;
	}>,
) {
	const canvasRef = useRef<HTMLCanvasElement | null>(null);
	const canvasContextRef = useRef<CanvasRenderingContext2D | undefined | null>(
		null,
	);

	const UNIT_HEXAGON_VERTICES: [number, number][] = Array.from(
		{ length: 6 },
		(_, i) => {
			const angle = (Math.PI / 3) * i + Math.PI / 6;
			return [Math.cos(angle), Math.sin(angle)];
		},
	);

	function drawHexagon(
		ctx: CanvasRenderingContext2D,
		x: number,
		y: number,
		sideLength: number,
		props: HexagonalTileProps = defaultTileProps,
	) {
		ctx.strokeStyle = props.stroke;
		ctx.lineWidth = +props.strokeWidth;
		ctx.lineJoin = "round";

		// Precompute vertices
		const vertices = UNIT_HEXAGON_VERTICES.map(([dx, dy]) => [
			x + sideLength * dx,
			y + sideLength * dy,
		]);

		ctx.beginPath();

		// Move to the first vertex
		ctx.moveTo(vertices[0][0], vertices[0][1]);

		for (let i = 0; i < 6; i++) {
			const next = (i + 1) % 6; // Wrap around for last vertex
			ctx.arcTo(
				vertices[i][0],
				vertices[i][1],
				vertices[next][0],
				vertices[next][1],
				cornerRadius,
			);
		}

		ctx.closePath();

		if (props.fill) {
			ctx.fillStyle = props.isDimmed
				? props.fill === "#FF006E"
					? "#800037"
					: "#008080"
				: props.fill;
			ctx.fill();
		}

		ctx.stroke();

		// Draw an 'X' inside the hexagon if requested
		if (props.textContent === "X") {
			ctx.strokeStyle = props.textColor;
			ctx.lineWidth = 1;

			// Define diagonal cross lines
			const x1 = x - sideLength * 0.6;
			const x2 = x + sideLength * 0.6;
			const y1 = y - (Math.sqrt(3) / 2) * sideLength * 0.6;
			const y2 = y + (Math.sqrt(3) / 2) * sideLength * 0.6;

			ctx.beginPath();
			ctx.moveTo(x1, y1);
			ctx.lineTo(x2, y2);
			ctx.moveTo(x1, y2);
			ctx.lineTo(x2, y1);
			ctx.stroke();
		}
	}

	const HORIZONTAL_PADDING = 50;
	const VERTICAL_PADDING = 50;
	const croppedWidth = canvasWidth - HORIZONTAL_PADDING;
	const croppedHeight = canvasHeight - VERTICAL_PADDING;
	const { rowCount, columnCount } = calcIdealColumnsAndRows(
		hex_count,
		croppedWidth,
		croppedHeight,
	);
	const {
		rightmost,
		bottommost,
		widthHexagon,
		heightHexagon,
		radiusToHexagonCorner,
	} = fitHexagonsToCanvas(
		hex_count,
		canvasWidth,
		canvasHeight,
		rowCount,
		columnCount,
	);

	const initialDrawn = useRef(false);

	useEffect(() => {
		if (canvasRef.current) {
			canvasContextRef.current = canvasRef.current.getContext("2d");
		}

		if (canvasContextRef.current && !initialDrawn.current) {
			initialDrawn.current = true;
			const initialDrawUpdates = Array.from(
				{ length: hex_count },
				(_, inx) => ({
					hexIndex: inx,
					newProps: { ...defaultTileProps },
				}),
			);
			drawChanges(initialDrawUpdates);
		}
	}, []);

	const drawChanges = (
		updates: {
			hexIndex: number;
			newProps: HexagonalTileProps;
		}[] = [],
	) => {
		const ctx = canvasContextRef.current;
		if (!ctx) {
			console.log("context inside canvas visu is null");
			return;
		}

		ctx.save();
		ctx.translate(
			(canvasWidth - rightmost) / 2,
			(canvasHeight - bottommost) / 2,
		);

		for (const update of updates) {
			const row = Math.floor(update.hexIndex / columnCount);
			const col = update.hexIndex % columnCount;
			if (row % 2 == 0) {
				drawHexagon(
					ctx,
					widthHexagon / 2 + widthHexagon * col,
					heightHexagon / 2 +
						((heightHexagon + radiusToHexagonCorner) * row) / 2,
					radiusToHexagonCorner * scale_factor_for_space_between_hexes,
					update.newProps,
				);
			} else {
				drawHexagon(
					ctx,
					widthHexagon / 2 + widthHexagon * (col + 0.5),
					heightHexagon / 2 +
						((heightHexagon + radiusToHexagonCorner) * row) / 2,
					radiusToHexagonCorner * scale_factor_for_space_between_hexes,
					update.newProps,
				);
			}
		}
		ctx.restore();
	};

	useImperativeHandle(ref, () => ({
		drawChanges,
	}));

	return (
		<>
			<div id="canvasContainer">
				<canvas
					id="visuCanvas"
					width={canvasWidth}
					height={canvasHeight}
					ref={canvasRef}
				>
					The visualization canvas
				</canvas>
			</div>
		</>
	);
});

export default CanvasVisu;
