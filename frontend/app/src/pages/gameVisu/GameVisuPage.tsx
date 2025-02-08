import HexagonalBoard from "@/components/hexagonalBoard/HexagonalBoard.tsx";
import "./gameVisu.css";
import Header from "@/components/header/Header.tsx";
import Footer from "@/components/footer/Footer.tsx";
import { useEffect, useRef, useState } from "react";
import { HexagonalTileProps } from "@/components/hexagonalTile/HexagonalTile.tsx";
import { LobbyStatus } from "@/domain/LobbyStatus.ts";
import { useLobby } from "@/services/lobbyContext/LobbyContextHelpers.ts";
import { getLobbyStatusV1WithVisuData } from "@/services/rest/LobbyRest.ts";
import { useNavigate } from "react-router-dom";
import { Separator } from "@/components/ui/separator.tsx";

// Not explicitly tested atm, because I dont have the time for it, this is mostly visual and we are nearing the deadline :(
function GameVisuPage() {
	const delay = (ms: number | undefined) =>
		new Promise((res) => setTimeout(res, ms));

	const [lobbyStatus, setLobbyStatus] = useState<LobbyStatus>();
	const lobby = useLobby();
	const navigate = useNavigate();
	const interval_time: number = 33;
	const counterRef = useRef(0);
	const [derivedValue, setDerivedValue] = useState(counterRef.current);

	const TILE_COUNT = 8192;
	const COLUMN_COUNT = 128;

	useEffect(() => {
		if (lobby) {
			getLobbyStatusV1WithVisuData(lobby.id).then((status) => {
				console.log("setting lobby status");
				setLobbyStatus(status);
				console.log("DONE setting lobby status");
			});
		}
	}, [lobby]);

	useEffect(() => {
		const interval = setInterval(async () => {
			if (lobbyStatus == undefined) {
				console.log("lobby status undefined");
				return;
			}
			if (counterRef.current >= lobbyStatus.visualizationData.length) {
				console.log("Done visualizing!");
				clearInterval(interval);
				await delay(3000);
				navigate("/result-display");
				return;
			}

			if (boardRef.current == null) {
				console.log("board ref undefined");
				return;
			}

			const previousIteration =
				counterRef.current >= 1
					? lobbyStatus.visualizationData[counterRef.current - 1]
					: null;
			const currentIteration =
				lobbyStatus.visualizationData[counterRef.current];

			const playerColor =
				currentIteration.playerId === "playerA" ? "#FF006E" : "#00FFFF";

			const updates: {
				row: number;
				col: number;
				props: Partial<HexagonalTileProps>;
			}[] = [];

			// Queue updates instead of applying directly
			for (const writeIndex of currentIteration.memoryWrites) {
				updates.push({
					row: Math.floor((writeIndex % TILE_COUNT) / COLUMN_COUNT),
					col: (writeIndex % TILE_COUNT) % COLUMN_COUNT,
					props: {
						fill: playerColor,
						textContent: "",
						isDimmed: true,
					},
				});
			}

			// Player reads
			for (const readIndex of currentIteration.memoryReads) {
				updates.push({
					row: Math.floor((readIndex % TILE_COUNT) / COLUMN_COUNT),
					col: (readIndex % TILE_COUNT) % COLUMN_COUNT,
					props: {
						textContent: "X",
						textColor: playerColor,
					},
				});
			}

			if (previousIteration != null) {
				// Turn off previous active process
				if (previousIteration.programCounterBefore >= 0) {
					updates.push({
						row: Math.floor(
							(previousIteration.programCounterBefore % TILE_COUNT) /
								COLUMN_COUNT,
						),
						col:
							(previousIteration.programCounterBefore % TILE_COUNT) %
							COLUMN_COUNT,
						props: {
							isDimmed: true,
							stroke: "",
						},
					});
				}

				// Move last active process
				if (previousIteration.programCounterAfter >= 0) {
					updates.push({
						row: Math.floor(
							(previousIteration.programCounterAfter % TILE_COUNT) /
								COLUMN_COUNT,
						),
						col:
							(previousIteration.programCounterAfter % TILE_COUNT) %
							COLUMN_COUNT,
						props: {
							stroke: "#BBBBBB",
							strokeWidth: "16",
						},
					});
				}
			}

			// Active process
			if (currentIteration.programCounterBefore >= 0) {
				updates.push({
					row: Math.floor(
						(currentIteration.programCounterBefore % TILE_COUNT) / COLUMN_COUNT,
					),
					col:
						(currentIteration.programCounterBefore % TILE_COUNT) % COLUMN_COUNT,
					props: {
						isDimmed: false,
						stroke: "white",
						strokeWidth: "48",
					},
				});
			}

			// Sleeping processes
			for (const sleepingProcess of currentIteration.programCountersOfOtherProcesses) {
				updates.push({
					row: Math.floor((sleepingProcess % TILE_COUNT) / COLUMN_COUNT),
					col: (sleepingProcess % TILE_COUNT) % COLUMN_COUNT,
					props: {
						stroke: "#BBBBBB",
						strokeWidth: "16",
					},
				});
			}

			boardRef.current.updateTiles(updates);

			counterRef.current++;
			setDerivedValue(counterRef.current);
		}, interval_time);

		// Clear interval on component unmount
		return () => clearInterval(interval);
		// this hook is supposed to trigger the interval / loop. adding further dependencies would create multiple instances of it
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [lobbyStatus]);

	const boardRef = useRef<{
		updateTiles: (
			updates: {
				row: number;
				col: number;
				props: Partial<HexagonalTileProps>;
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
					<button onClick={() => navigate("/result-display")}>
						{"skip visualization>>"}
					</button>
				</div>
			</div>
			<div id={"visuBoard"}>
				<HexagonalBoard
					rows={64}
					tilesPerRow={128}
					ref={boardRef}
				></HexagonalBoard>
			</div>
			<Footer />
		</div>
	);
}

export default GameVisuPage;
