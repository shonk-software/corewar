import { useEffect, useRef, useState } from "react";
import CanvasVisu from "@/components/canvasVisu/CanvasVisu";
import { LobbyStatus } from "@/domain/LobbyStatus.ts";
import { useLobby } from "@/services/lobbyContext/LobbyContextHelpers.ts";
import { useNavigate } from "react-router-dom";
import { getLobbyStatusV1WithVisuData } from "@/services/rest/LobbyRest.ts";
import { defaultTileProps } from "@/lib/DefaultTileProps.ts";
import Header from "@/components/header/Header.tsx";
import { Separator } from "@/components/ui/separator.tsx";
import Footer from "@/components/footer/Footer.tsx";
import "./canvasGameVisu.css";
import { HexagonalTileProps } from "@/domain/HexagonalTileProps.tsx";

function CanvasGameVisuPage() {
	const lobbyStatus = useRef<LobbyStatus | null>(null);
	const lobby = useLobby();
	const navigate = useNavigate();
	const counterRef = useRef(0);
	const [derivedValue, setDerivedValue] = useState(counterRef.current);

	const hex_count = 8192;
	const isDrawing = useRef(false);

	useEffect(() => {
		const startDraw = () => {
			if (lobbyStatus.current == undefined) {
				console.log("lobby status undefined");
			}

			const stateStore = Array.from({ length: hex_count }, () => ({
				...defaultTileProps,
			}));

			// let drawAttempts = 0;
			function drawLoop() {
				if (lobbyStatus.current == undefined) {
					console.log("lobby status undefined");
					return;
				}

				if (boardRef.current == null) {
					return;
				}

				if (
					counterRef.current >= lobbyStatus.current.visualizationData.length
				) {
					console.log("Done visualizing!");
					setTimeout(() => navigate("/result-display"), 500000000000000000000);
					return;
				}

				console.log(lobbyStatus.current.visualizationData);

				const touchedTiles = new Set<number>();
				const visuStep = [];

				for (let i = 0; i < 50; ++i) {
					if (
						counterRef.current >= lobbyStatus.current.visualizationData.length
					) {
						console.log(
							"reached end of visu during fast forward, ending early...",
						);

						for (const index of touchedTiles.values()) {
							visuStep.push({
								hexIndex: index,
								newProps: stateStore[index],
							});
						}
						counterRef.current++;
						boardRef.current.drawChanges(visuStep);

						setTimeout(() => navigate("/result-display"), 3000);
						return;
					}
					const logicalStepData =
						lobbyStatus.current.visualizationData[counterRef.current];
					const previousStepData =
						counterRef.current > 0
							? lobbyStatus.current.visualizationData[counterRef.current - 1]
							: null;

					const playerColor =
						logicalStepData.playerId === "playerA" ? "#FF006E" : "#00FFFF";

					// todo check if Object.assign is an alternative
					for (const writeIndex of logicalStepData.memoryWrites) {
						touchedTiles.add(writeIndex);
						stateStore[writeIndex] = {
							...stateStore[writeIndex],
							fill: playerColor,
							textContent: "",
							isDimmed: true,
						};
					}

					// Player reads
					for (const readIndex of logicalStepData.memoryReads) {
						touchedTiles.add(readIndex);
						stateStore[readIndex] = {
							...stateStore[readIndex],
							textContent: "X",
							textColor: playerColor,
						};
					}

					if (previousStepData != null) {
						// Turn off previous active process
						if (previousStepData.programCounterBefore >= 0) {
							touchedTiles.add(previousStepData.programCounterBefore);
							stateStore[previousStepData.programCounterBefore] = {
								...stateStore[previousStepData.programCounterBefore],
								isDimmed: true,
								stroke: "#808080",
								strokeWidth: "1",
							};
						}

						// Move last active process if it hasnt died
						if (
							previousStepData.programCounterAfter >= 0 &&
							!previousStepData.processDied
						) {
							touchedTiles.add(previousStepData.programCounterAfter);
							stateStore[previousStepData.programCounterAfter] = {
								...stateStore[previousStepData.programCounterAfter],
								stroke: "#505050",
								strokeWidth: "1",
							};
						}
					}

					// Active process
					if (logicalStepData.programCounterBefore >= 0) {
						touchedTiles.add(logicalStepData.programCounterBefore);
						stateStore[logicalStepData.programCounterBefore] = {
							...stateStore[logicalStepData.programCounterBefore],
							isDimmed: false,
							stroke: "#FFFFFF",
							strokeWidth: "1",
						};
					}

					// Sleeping processes
					for (const sleepingProcess of logicalStepData.programCountersOfOtherProcesses) {
						touchedTiles.add(sleepingProcess);
						stateStore[sleepingProcess] = {
							...stateStore[sleepingProcess],
							stroke: "#808080",
							strokeWidth: "1",
						};
					}

					// Merge all visuStep updates together, result should be a list where each updated tile is only included once
					// and its props are complete
					for (const index of touchedTiles.values()) {
						visuStep.push({
							hexIndex: index % hex_count,
							newProps: stateStore[index],
						});
					}
					counterRef.current++;
					setDerivedValue(counterRef.current);
				}
				boardRef.current.drawChanges(visuStep);
				requestAnimationFrame(drawLoop);
			}
			requestAnimationFrame(drawLoop);
		};

		if (lobby) {
			getLobbyStatusV1WithVisuData(lobby.id).then((status) => {
				console.log("setting lobby status");
				lobbyStatus.current = status;
				console.log("DONE setting lobby status");
				if (!isDrawing.current) {
					isDrawing.current = true;
					setTimeout(startDraw, 1000);
				}
			});
		}
	}, [lobby, navigate]);

	const boardRef = useRef<{
		drawChanges: (
			updates: {
				hexIndex: number;
				newProps: HexagonalTileProps;
			}[],
		) => void;
	}>(null);

	return (
		<div id={"everything"}>
			<Header />
			<div id={"boardBuffer"}>
				<div id={"infodump"}>
					Infodump:
					<Separator
						style={{ height: "2px", background: "#FFFFFF" }}
					></Separator>
					<span>
						<b>Board size: </b>8192 cells
					</span>
					<span>
						<b>Current cycle: </b>
						{derivedValue}
					</span>
					<span>
						<b>Max cycles: </b>10.000
					</span>
				</div>
				<div id={"theRestOfTheBoard"}>
					<button
						onClick={() => navigate("/result-display")}
						style={{ zIndex: 10 }}
					>
						{"skip visualization>>"}
					</button>
				</div>
			</div>
			<div id={"visuBoard"}>
				<CanvasVisu
					defaultTileProps={defaultTileProps}
					ref={boardRef}
					canvasWidth={1500}
					canvasHeight={900}
					hex_count={hex_count}
					scale_factor_for_space_between_hexes={0.9}
					cornerRadius={0}
				></CanvasVisu>
			</div>
			<Footer />
		</div>
	);
}

export default CanvasGameVisuPage;
