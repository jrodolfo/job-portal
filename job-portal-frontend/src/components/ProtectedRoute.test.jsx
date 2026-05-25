import {screen, waitFor} from "@testing-library/react";
import {Routes, Route} from "react-router-dom";
import axios from "axios";
import ProtectedRoute from "./ProtectedRoute";
import {renderWithProviders} from "../test/test-utils";

vi.mock("axios");

const renderProtectedRoute = ({route = "/admin-dashboard", preloadedState} = {}) =>
    renderWithProviders(
        <Routes>
            <Route path="/" element={<div>Login Screen</div>} />
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
                username: "admin",
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
                username: "user",
                roles: ["ROLE_APPLICANT"]
            }
        });

        renderProtectedRoute();

        expect(await screen.findByText("Applicant Screen")).toBeInTheDocument();
    });
});
