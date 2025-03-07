import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

export function calcIdealColumnsAndRows(
	hexCount: number,
	width: number,
	height: number,
) {
	const ratio = width / height;
	const startRows = Math.sqrt(hexCount / ratio);
	const startCols = hexCount / startRows;

	let bestRows = 0,
		bestCols = 0,
		minOverhead = Infinity;

	for (
		let rows = Math.floor(startRows * 0.8);
		rows <= Math.ceil(startRows * 1.2);
		rows++
	) {
		for (
			let cols = Math.floor(startCols * 0.8);
			cols <= Math.ceil(startCols * 1.2);
			cols++
		) {
			const totalHexes = rows * cols;
			const overhead = totalHexes - hexCount;

			if (totalHexes === hexCount) return { rowCount: rows, columnCount: cols };

			if (overhead >= 0 && overhead < minOverhead) {
				bestRows = rows;
				bestCols = cols;
				minOverhead = overhead;
			}
		}
	}

	return { rowCount: bestRows, columnCount: bestCols };
}

export function calculateHexagonDimensions(areaPerHex: number) {
	const radiusToHexagonCorner = Math.sqrt(
		(areaPerHex * 4) / (6 * Math.sqrt(3)),
	);
	const heightHexagon = 2 * radiusToHexagonCorner;
	const widthHexagon = radiusToHexagonCorner * Math.cos(Math.PI / 6) * 2; // 30 degrees in radians
	return { radiusToHexagonCorner, widthHexagon, heightHexagon };
}

export function fitHexagonsToCanvas(
	hexCount: number,
	canvasWidth: number,
	canvasHeight: number,
	rowCount: number,
	columnCount: number,
): {
	rightmost: number;
	bottommost: number;
	widthHexagon: number;
	heightHexagon: number;
	radiusToHexagonCorner: number;
} {
	const croppedWidth = canvasWidth - 100;
	const croppedHeight = canvasHeight - 100;
	const canvasArea = croppedWidth * croppedHeight;

	let areaFactor = 1.0;
	let hexDims = calculateHexagonDimensions(
		(canvasArea / hexCount) * areaFactor,
	);
	let rightmost = 0,
		bottommost = 0;

	do {
		hexDims = calculateHexagonDimensions((canvasArea / hexCount) * areaFactor);
		const { widthHexagon, heightHexagon, radiusToHexagonCorner } = hexDims;

		rightmost = (columnCount + 0.5) * widthHexagon;
		bottommost =
			(rowCount - 1) * (3 / 2) * radiusToHexagonCorner + heightHexagon; // ✅ Corrected and properly scoped

		areaFactor -= 0.01;
	} while (rightmost > croppedWidth || bottommost > croppedHeight);

	return { rightmost, bottommost, ...hexDims };
}
