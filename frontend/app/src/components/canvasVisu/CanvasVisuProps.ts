import { HexagonalTileProps } from "@/domain/HexagonalTileProps.tsx";

export interface CanvasVisuProps {
	defaultTileProps: HexagonalTileProps;
	canvasWidth: number;
	canvasHeight: number;
	hex_count: number;
	scale_factor_for_space_between_hexes: number;
	cornerRadius: number;
}
