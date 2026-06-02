import {screen, waitFor} from "@testing-library/react";
import {Route, Routes, useLocation} from "react-router-dom";
import axios from "axios";
import ProtectedRoute from "./ProtectedRoute";
import {renderWithProviders} from "../test/test-utils";

vi.mock("axios");

const LoginScreen = () => {
    const location = useLocation();
    return (
        <div>
            <div>Login Screen</div>
            {location.state?.sessionMessage ? <div role="status">{location.state.sessionMessage}</div> : null}
        </div>
    );
};

const renderProtectedRoute = ({route = "/admin-dashboard", preloadedState} = {}) =>
    renderWithProviders(
        <Routes>
            <Route path="/" element={<LoginScreen/>}/>
            <Route
                path="/applicant-dashboard"
                element={<div>Applicant Screen</div>}
            />
            <Route
                path="/admin-dashboard"
                element={
                    <ProtectedRoute requiredRole="ROLE_ADMIN">
                        <div>Admin Screen</div>
                    </ProtectedRoute>
                }
            />
        </Routes>,
        {route, preloadedState}
    );

describe("ProtectedRoute", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it("should redirect to login when no token is present", async () => {
        renderProtectedRoute();
        expect(await screen.findByText("Login Screen")).toBeInTheDocument();
    });

    it("should hydrate user details from the token and allow admin access", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get.mockResolvedValueOnce({
            data: {
                username: "admin@local.test",
                displayName: "Admin User",
                roles: ["ROLE_ADMIN"]
            }
        });

        renderProtectedRoute();

        await waitFor(() =>
            expect(axios.get).toHaveBeenCalledWith(
                "http://localhost:8080/api/auth/details",
                {
                    headers: {
                        Authorization: "Bearer jwt-admin"
                    }
                }
            )
        );
        expect(await screen.findByText("Admin Screen")).toBeInTheDocument();
    });

    it("should redirect applicant users away from admin routes", async () => {
        localStorage.setItem("token", "jwt-user");
        axios.get.mockResolvedValueOnce({
            data: {
                username: "user@local.test",
                displayName: "Applicant User",
                roles: ["ROLE_APPLICANT"]
            }
        });

        renderProtectedRoute();

        expect(await screen.findByText("Applicant Screen")).toBeInTheDocument();
    });

    it("should clear session and redirect with a message when token hydration returns unauthorized", async () => {
        localStorage.setItem("token", "jwt-user");
        axios.get.mockRejectedValueOnce({
            response: {
                status: 401,
                data: {
                    message: "Your account is disabled. Please contact an administrator."
                }
            }
        });

        const {store} = renderProtectedRoute();

        expect(await screen.findByText("Login Screen")).toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent("Your account is disabled. Please contact an administrator.");
        expect(localStorage.getItem("token")).toBeNull();
        expect(store.getState().user).toEqual({
            username: "",
            displayName: "",
            role: ""
        });
    });
});
