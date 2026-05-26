import {screen, waitFor} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import axios from "axios";
import AdminDashboard from "./AdminDashboard";
import {renderWithProviders} from "../test/test-utils";

vi.mock("axios");
vi.mock("./Navbar", () => ({
    default: () => <div data-testid="navbar-mock">Navbar</div>
}));

describe("AdminDashboard", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it("should fetch and render jobs on mount", async () => {
        axios.get.mockResolvedValueOnce({
            data: [
                {
                    id: 1,
                    title: "Java Developer",
                    description: "Build APIs",
                    company: "ACME",
                    postedDate: "2026-01-01"
                }
            ]
        });

        renderWithProviders(<AdminDashboard />);

        await waitFor(() => expect(axios.get).toHaveBeenCalledWith("http://localhost:8080/api/jobs"));
        expect(screen.getByText("Add New Job")).toBeInTheDocument();
        expect(screen.getByText("Title: Java Developer")).toBeInTheDocument();
    });

    it("should send create job request with bearer token and prepend the new job", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get.mockResolvedValueOnce({data: []});
        axios.post.mockResolvedValueOnce({
            data: {
                id: 10,
                title: "Platform Engineer",
                description: "Own internal services",
                company: "ACME",
                postedDate: "2026-05-25"
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await user.type(screen.getByLabelText("Title"), "Platform Engineer");
        await user.type(screen.getByLabelText("Company"), "ACME");
        await user.type(screen.getByLabelText("Description"), "Own internal services");
        await user.click(screen.getByRole("button", {name: "Create Job"}));

        await waitFor(() =>
            expect(axios.post).toHaveBeenCalledWith(
                "http://localhost:8080/api/jobs",
                {
                    title: "Platform Engineer",
                    description: "Own internal services",
                    company: "ACME"
                },
                {
                    headers: {
                        Authorization: "Bearer jwt-admin"
                    }
                }
            )
        );

        expect(await screen.findByText("Job created successfully.")).toBeInTheDocument();
        expect(screen.getByText("Title: Platform Engineer")).toBeInTheDocument();
    });

    it("should enter edit mode and send update request with bearer token", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get.mockResolvedValueOnce({
            data: [
                {
                    id: 1,
                    title: "Java Developer",
                    description: "Build APIs",
                    company: "ACME",
                    postedDate: "2026-05-25"
                }
            ]
        });
        axios.put.mockResolvedValueOnce({
            data: {
                id: 1,
                title: "Senior Java Developer",
                description: "Build platform APIs",
                company: "ACME",
                postedDate: "2026-05-25"
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole("button", {name: "Edit"})).toBeInTheDocument());
        await user.click(screen.getByRole("button", {name: "Edit"}));

        const titleInput = screen.getByLabelText("Title");
        await user.clear(titleInput);
        await user.type(titleInput, "Senior Java Developer");

        const descriptionInput = screen.getByLabelText("Description");
        await user.clear(descriptionInput);
        await user.type(descriptionInput, "Build platform APIs");

        await user.click(screen.getByRole("button", {name: "Save Changes"}));

        await waitFor(() =>
            expect(axios.put).toHaveBeenCalledWith(
                "http://localhost:8080/api/jobs/1",
                {
                    title: "Senior Java Developer",
                    description: "Build platform APIs",
                    company: "ACME"
                },
                {
                    headers: {
                        Authorization: "Bearer jwt-admin"
                    }
                }
            )
        );

        expect(await screen.findByText("Job updated successfully.")).toBeInTheDocument();
        expect(screen.getByText("Title: Senior Java Developer")).toBeInTheDocument();
    });

    it("should show backend validation message when create fails with bad request", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get.mockResolvedValueOnce({data: []});
        axios.post.mockRejectedValueOnce({
            response: {
                status: 400,
                data: {
                    message: "Title is required"
                }
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await user.type(screen.getByLabelText("Title"), " ");
        await user.type(screen.getByLabelText("Company"), "ACME");
        await user.type(screen.getByLabelText("Description"), "Own internal services");
        await user.click(screen.getByRole("button", {name: "Create Job"}));

        expect(await screen.findByText("Title is required")).toBeInTheDocument();
    });

    it("should show backend validation message when update fails with bad request", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get.mockResolvedValueOnce({
            data: [
                {
                    id: 1,
                    title: "Java Developer",
                    description: "Build APIs",
                    company: "ACME",
                    postedDate: "2026-05-25"
                }
            ]
        });
        axios.put.mockRejectedValueOnce({
            response: {
                status: 400,
                data: {
                    message: "Description must be at most 2000 characters"
                }
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole("button", {name: "Edit"})).toBeInTheDocument());
        await user.click(screen.getByRole("button", {name: "Edit"}));
        await user.click(screen.getByRole("button", {name: "Save Changes"}));

        expect(await screen.findByText("Description must be at most 2000 characters")).toBeInTheDocument();
    });

    it("should delete a job after confirmation", async () => {
        localStorage.setItem("token", "jwt-admin");
        vi.stubGlobal("confirm", vi.fn(() => true));
        axios.get.mockResolvedValueOnce({
            data: [
                {
                    id: 1,
                    title: "Java Developer",
                    description: "Build APIs",
                    company: "ACME",
                    postedDate: "2026-05-25"
                }
            ]
        });
        axios.delete.mockResolvedValueOnce({});

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole("button", {name: "Delete"})).toBeInTheDocument());
        await user.click(screen.getByRole("button", {name: "Delete"}));

        await waitFor(() =>
            expect(axios.delete).toHaveBeenCalledWith(
                "http://localhost:8080/api/jobs/1",
                {
                    headers: {
                        Authorization: "Bearer jwt-admin"
                    }
                }
            )
        );

        expect(await screen.findByText("Job deleted successfully.")).toBeInTheDocument();
        expect(screen.queryByText("Title: Java Developer")).not.toBeInTheDocument();
    });

    it("should show the backend conflict message when deleting a job with applications", async () => {
        localStorage.setItem("token", "jwt-admin");
        vi.stubGlobal("confirm", vi.fn(() => true));
        axios.get.mockResolvedValueOnce({
            data: [
                {
                    id: 1,
                    title: "Java Developer",
                    description: "Build APIs",
                    company: "ACME",
                    postedDate: "2026-05-25"
                }
            ]
        });
        axios.delete.mockRejectedValueOnce({
            response: {
                status: 409,
                data: {
                    message: "Cannot delete job with existing applications"
                }
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole("button", {name: "Delete"})).toBeInTheDocument());
        await user.click(screen.getByRole("button", {name: "Delete"}));

        expect(await screen.findByText("Cannot delete job with existing applications")).toBeInTheDocument();
        expect(screen.getByText("Title: Java Developer")).toBeInTheDocument();
    });
});
