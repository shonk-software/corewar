import {
	aLobby,
	mockResultDraw,
	mockResultWinnerA,
	mockResultWinnerB,
} from "@/TestFactories";
import {
	useDispatchLobby,
	useLobby,
} from "@/services/lobbyContext/LobbyContextHelpers";
import { getLobbyStatusV1WithoutVisuData } from "@/services/rest/LobbyRest";
import { playerA, useUser } from "@/services/userContext/UserContextHelpers";
import "@testing-library/jest-dom";
import {
	act,
	fireEvent,
	render,
	screen,
	waitFor,
} from "@testing-library/react";
import { createMemoryRouter, Navigate, RouterProvider } from "react-router-dom";
import { afterEach, describe, expect, it, Mock, vi } from "vitest";
import ResultDisplayPage from "./ResultDisplayPage";

// Mock the getStatusV0 function
vi.mock("@/services/rest/LobbyRest", () => ({
	getLobbyStatusV1WithoutVisuData: vi.fn(),
}));
vi.mock("@/services/userContext/UserContextHelpers");

vi.mock("@/services/lobbyContext/LobbyContextHelpers");

const testRouterConfig = [
	{
		path: "/",
		element: <Navigate to="/result-display" replace={true} />,
	},
	{
		path: "/result-display",
		element: <ResultDisplayPage />,
	},
	{
		path: "/lobby-selection",
		element: <div>hehe :3</div>,
	},
];

describe("ResultDisplayPage", () => {
	afterEach(() => {
		vi.clearAllMocks();
	});

	it("should make an API request and display the result", async () => {
		const expectedLobby = aLobby();
		(useUser as Mock).mockReturnValue(playerA);
		(useLobby as Mock).mockReturnValue(expectedLobby);
		const router = createMemoryRouter(testRouterConfig);

		(getLobbyStatusV1WithoutVisuData as Mock).mockResolvedValue(
			mockResultWinnerA,
		);

		act(() => {
			render(<RouterProvider router={router} />);
		});

		await waitFor(() => {
			expect(getLobbyStatusV1WithoutVisuData).toHaveBeenCalled();
		});
	});

	it("should display 'You won!' when the user is the winner", async () => {
		(useUser as Mock).mockReturnValue(playerA);
		(useLobby as Mock).mockReturnValue(aLobby());
		const router = createMemoryRouter(testRouterConfig);

		(getLobbyStatusV1WithoutVisuData as Mock).mockResolvedValue(
			mockResultWinnerA,
		);

		act(() => {
			render(<RouterProvider router={router} />);
		});

		await waitFor(() => {
			expect(screen.getByText("You won!")).toBeInTheDocument();
			expect(screen.getByAltText("playerA icon")).toBeInTheDocument();
		});
	});

	it("should display 'You lost!' when the user is not the winner", async () => {
		(useUser as Mock).mockReturnValue(playerA);
		(useLobby as Mock).mockReturnValue(aLobby());
		const router = createMemoryRouter(testRouterConfig);

		(getLobbyStatusV1WithoutVisuData as Mock).mockResolvedValue(
			mockResultWinnerB,
		);

		act(() => {
			render(<RouterProvider router={router} />);
		});

		await waitFor(() => {
			expect(screen.getByText("You lost!")).toBeInTheDocument();
			expect(screen.getByAltText("playerB icon")).toBeInTheDocument();
		});
	});

	it("should display 'It's a draw!' when the result is a draw", async () => {
		(useUser as Mock).mockReturnValue(playerA);
		(useLobby as Mock).mockReturnValue(aLobby());
		const router = createMemoryRouter(testRouterConfig);

		(getLobbyStatusV1WithoutVisuData as Mock).mockResolvedValue(mockResultDraw);

		act(() => {
			render(<RouterProvider router={router} />);
		});

		await waitFor(() => {
			expect(screen.getByText("It's a draw!")).toBeInTheDocument();
			expect(screen.getByAltText("draw icon")).toBeInTheDocument();
		});
	});

	it("should navigate to lobby selection page and leave lobby on button click", async () => {
		const mockLobbyReducer = vi.fn();
		(useDispatchLobby as Mock).mockReturnValue(mockLobbyReducer);

		const router = createMemoryRouter(testRouterConfig);

		(getLobbyStatusV1WithoutVisuData as Mock).mockResolvedValue({
			ok: true,
			json: async () => mockResultWinnerA,
		});

		act(() => {
			render(<RouterProvider router={router} />);
		});
		act(() => {
			const button = screen.getByRole("button", { name: /Play again/i });
			fireEvent.click(button);
		});
		expect(router.state.location.pathname).toEqual("/lobby-selection");
		expect(mockLobbyReducer).toHaveBeenCalledWith({
			type: "leave",
			lobby: null,
		});
	});
});
