import {screen, waitFor} from "@testing-library/react";
import {within} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import axios from "axios";
import AdminDashboard from "./AdminDashboard";
import {renderWithProviders} from "../test/test-utils";

vi.mock("axios");
vi.mock("./Navbar", () => ({
    default: () => <div data-testid="navbar-mock">Navbar</div>
}));

const jobDateTimeFormatter = new Intl.DateTimeFormat(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit"
});

const openAdminTab = async (user, name) => {
    await user.click(screen.getByRole("tab", {name}));
};

const openAddJobTab = async (user) => {
    await openAdminTab(user, "Add Job");
};

const openApplicationsTab = async (user) => {
    await openAdminTab(user, "Applications");
};

const byTextContent = (text) => (_, element) => element?.textContent === text;

describe("AdminDashboard", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it("should fetch and render jobs on mount", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-01-01",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "user"},
                        job: {id: 1, title: "Java Developer"}
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);

        await waitFor(() => expect(axios.get).toHaveBeenCalledWith("http://localhost:8080/api/jobs/admin", {
            headers: {
                Authorization: "Bearer jwt-admin"
            }
        }));
        expect(axios.get).toHaveBeenCalledWith("http://localhost:8080/api/applications", {
            headers: {
                Authorization: "Bearer jwt-admin"
            }
        });
        expect(screen.getByRole("heading", {name: "Admin"})).toBeInTheDocument();
        expect(screen.getByRole("tab", {name: "Jobs"})).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("tab", {name: "Add Job"})).toBeInTheDocument();
        expect(screen.getByRole("tab", {name: "Applications"})).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Java Developer"))).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Status: Open"))).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Applications: 1"))).toBeInTheDocument();
    });

    it("should switch between jobs, add job, and applications tabs", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-01-01",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "user"},
                        job: {id: 1, title: "Java Developer"}
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByText(byTextContent("Java Developer"))).toBeInTheDocument());
        expect(screen.getByRole("tab", {name: "Jobs"})).toHaveAttribute("aria-selected", "true");

        await openAddJobTab(user);
        expect(screen.getByRole("heading", {name: "Add New Job"})).toBeInTheDocument();
        expect(screen.getByLabelText("Title")).toBeInTheDocument();

        await openApplicationsTab(user);
        expect(screen.getByRole("heading", {name: "Applied (1)"})).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Applicant: user"))).toBeInTheDocument();
    });

    it("should show the posted date with time when a timestamp is available", async () => {
        localStorage.setItem("token", "jwt-admin");
        const createdAt = "2026-05-26T14:43:00Z";
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-26",
                        createdAt,
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});

        renderWithProviders(<AdminDashboard />);

        const expectedDateTime = jobDateTimeFormatter.format(new Date(createdAt));
        await waitFor(() => expect(screen.getByText(byTextContent(`Posted Date: ${expectedDateTime}`))).toBeInTheDocument());
    });

    it("should send create job request with bearer token and prepend the new job", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({data: []});
        axios.post.mockResolvedValueOnce({
            data: {
                id: 10,
                title: "Platform Engineer",
                description: "Own internal services",
                company: "ACME",
                postedDate: "2026-05-25",
                status: "OPEN"
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openAddJobTab(user);
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
        expect(screen.getByText(byTextContent("Platform Engineer"))).toBeInTheDocument();
        expect(screen.getByRole("tab", {name: "Jobs"})).toHaveAttribute("aria-selected", "true");
    });

    it("should enter edit mode and send update request with bearer token", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});
        axios.put.mockResolvedValueOnce({
            data: {
                id: 1,
                title: "Senior Java Developer",
                description: "Build platform APIs",
                company: "ACME",
                postedDate: "2026-05-25",
                status: "OPEN"
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole("button", {name: "Edit"})).toBeInTheDocument());
        await user.click(screen.getByRole("button", {name: "Edit"}));
        expect(screen.getByRole("tab", {name: "Add Job"})).toHaveAttribute("aria-selected", "true");

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
        expect(screen.getByText(byTextContent("Senior Java Developer"))).toBeInTheDocument();
        expect(screen.getByRole("tab", {name: "Jobs"})).toHaveAttribute("aria-selected", "true");
    });

    it("should show backend validation message when create fails with bad request", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({data: []});
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

        await openAddJobTab(user);
        await user.type(screen.getByLabelText("Title"), " ");
        await user.type(screen.getByLabelText("Company"), "ACME");
        await user.type(screen.getByLabelText("Description"), "Own internal services");
        await user.click(screen.getByRole("button", {name: "Create Job"}));

        expect(await screen.findByText("Title is required")).toBeInTheDocument();
    });

    it("should show backend validation message when update fails with bad request", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});
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
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});
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
        expect(screen.queryByText(byTextContent("Java Developer"))).not.toBeInTheDocument();
    });

    it("should show the backend conflict message when deleting a job with applications", async () => {
        localStorage.setItem("token", "jwt-admin");
        vi.stubGlobal("confirm", vi.fn(() => true));
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});
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
        expect(screen.getByText(byTextContent("Java Developer"))).toBeInTheDocument();
    });

    it("should close and reopen a job with the admin status action", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});
        axios.put
            .mockResolvedValueOnce({
                data: {
                    id: 1,
                    title: "Java Developer",
                    description: "Build APIs",
                    company: "ACME",
                    postedDate: "2026-05-25",
                    status: "CLOSED"
                }
            })
            .mockResolvedValueOnce({
                data: {
                    id: 1,
                    title: "Java Developer",
                    description: "Build APIs",
                    company: "ACME",
                    postedDate: "2026-05-25",
                    status: "OPEN"
                }
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole("button", {name: "Close"})).toBeInTheDocument());
        await user.click(screen.getByRole("button", {name: "Close"}));

        await waitFor(() =>
            expect(axios.put).toHaveBeenCalledWith(
                "http://localhost:8080/api/jobs/1/status",
                null,
                {
                    params: {
                        status: "CLOSED"
                    },
                    headers: {
                        Authorization: "Bearer jwt-admin"
                    }
                }
            )
        );

        expect(await screen.findByText("Job closed successfully.")).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Status: Closed"))).toBeInTheDocument();
        expect(screen.getByRole("button", {name: "Reopen"})).toBeInTheDocument();

        const closedCard = screen.getByText(byTextContent("Java Developer")).closest(".job-card");
        expect(closedCard).toHaveClass("job-card-closed");

        await user.click(screen.getByRole("button", {name: "Reopen"}));

        await waitFor(() =>
            expect(axios.put).toHaveBeenCalledWith(
                "http://localhost:8080/api/jobs/1/status",
                null,
                {
                    params: {
                        status: "OPEN"
                    },
                    headers: {
                        Authorization: "Bearer jwt-admin"
                    }
                }
            )
        );

        expect(await screen.findByText("Job reopened successfully.")).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Status: Open"))).toBeInTheDocument();
        expect(screen.getByRole("button", {name: "Close"})).toBeInTheDocument();
        const reopenedCard = screen.getByText(byTextContent("Java Developer")).closest(".job-card");
        expect(reopenedCard).toHaveClass("job-card-open");
    });

    it("should filter jobs by status", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    },
                    {
                        id: 2,
                        title: "QA Engineer",
                        description: "Test releases",
                        company: "Globex",
                        postedDate: "2026-05-26",
                        status: "CLOSED"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByLabelText("Job Status")).toBeInTheDocument());
        await user.selectOptions(screen.getByLabelText("Job Status"), "CLOSED");

        expect(screen.getByText(byTextContent("QA Engineer"))).toBeInTheDocument();
        expect(screen.queryByText(byTextContent("Java Developer"))).not.toBeInTheDocument();
    });

    it("should filter jobs by search term", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    },
                    {
                        id: 2,
                        title: "QA Engineer",
                        description: "Test releases",
                        company: "Globex",
                        postedDate: "2026-05-26",
                        status: "CLOSED"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByLabelText("Search Jobs")).toBeInTheDocument());
        await user.type(screen.getByLabelText("Search Jobs"), "glob");

        expect(screen.getByText(byTextContent("QA Engineer"))).toBeInTheDocument();
        expect(screen.queryByText(byTextContent("Java Developer"))).not.toBeInTheDocument();
    });

    it("should sort jobs by oldest first", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Newer Job",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-26",
                        status: "OPEN"
                    },
                    {
                        id: 2,
                        title: "Older Job",
                        description: "Test releases",
                        company: "Globex",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByLabelText("Job Sort")).toBeInTheDocument());
        await user.selectOptions(screen.getByLabelText("Job Sort"), "oldest");

        const headings = screen.getAllByRole("heading", {level: 4}).map((element) => element.textContent);
        expect(headings[0]).toBe("Older Job");
        expect(headings[1]).toBe("Newer Job");
    });

    it("should show an empty state when no jobs match the current filters", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25",
                        status: "OPEN"
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByLabelText("Search Jobs")).toBeInTheDocument());
        await user.type(screen.getByLabelText("Search Jobs"), "does-not-match");

        expect(screen.getByText("No jobs match the current filters.")).toBeInTheDocument();
        expect(screen.queryByText(byTextContent("Java Developer"))).not.toBeInTheDocument();
    });

    it("should update an application status and refresh the admin list", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: "Java Developer",
                        description: "Build APIs",
                        company: "ACME",
                        postedDate: "2026-05-25"
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "user"},
                        job: {id: 1, title: "Java Developer"}
                    }
                ]
            });
        axios.put.mockResolvedValueOnce({
            data: {
                id: 10,
                status: "REVIEWING",
                user: {name: "user"},
                job: {id: 1, title: "Java Developer"}
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByLabelText("Status")).toBeInTheDocument());
        await user.selectOptions(screen.getByLabelText("Status"), "REVIEWING");
        await user.click(screen.getByRole("button", {name: "Save"}));

        await waitFor(() =>
            expect(axios.put).toHaveBeenCalledWith(
                "http://localhost:8080/api/applications/10",
                null,
                {
                    params: {
                        status: "REVIEWING"
                    },
                    headers: {
                        Authorization: "Bearer jwt-admin"
                    }
                }
            )
        );

        expect(await screen.findByText("Application status updated successfully.")).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Current Status: Reviewing"))).toBeInTheDocument();
    });

    it("should filter applications by status", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "alice"},
                        job: {id: 1, title: "Java Developer"},
                        createdAt: "2026-05-25T10:00:00"
                    },
                    {
                        id: 11,
                        status: "REVIEWING",
                        user: {name: "bob"},
                        job: {id: 2, title: "QA Engineer"},
                        createdAt: "2026-05-26T10:00:00"
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByLabelText("Filter by Status")).toBeInTheDocument());
        await user.selectOptions(screen.getByLabelText("Filter by Status"), "REVIEWING");

        expect(screen.getByRole("heading", {name: "Reviewing (1)"})).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Applicant: bob"))).toBeInTheDocument();
        expect(screen.queryByText(byTextContent("Applicant: alice"))).not.toBeInTheDocument();
    });

    it("should filter applications by search term", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "alice"},
                        job: {id: 1, title: "Java Developer"},
                        createdAt: "2026-05-25T10:00:00"
                    },
                    {
                        id: 11,
                        status: "REVIEWING",
                        user: {name: "bob"},
                        job: {id: 2, title: "QA Engineer"},
                        createdAt: "2026-05-26T10:00:00"
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByLabelText("Search Applications")).toBeInTheDocument());
        await user.type(screen.getByLabelText("Search Applications"), "qa");

        expect(screen.getByRole("heading", {name: "Reviewing (1)"})).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Applicant: bob"))).toBeInTheDocument();
        expect(screen.queryByText(byTextContent("Applicant: alice"))).not.toBeInTheDocument();
    });

    it("should group applications by status with counts", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "alice"},
                        job: {id: 1, title: "Java Developer"},
                        createdAt: "2026-05-26T10:00:00"
                    },
                    {
                        id: 11,
                        status: "REVIEWING",
                        user: {name: "bob"},
                        job: {id: 2, title: "QA Engineer"},
                        createdAt: "2026-05-25T10:00:00"
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByRole("heading", {name: "Reviewing (1)"})).toBeInTheDocument());
        expect(screen.getByRole("heading", {name: "Applied (1)"})).toBeInTheDocument();
        expect(screen.getByRole("heading", {name: "Reviewing (1)"})).toBeInTheDocument();
        const appliedCard = screen.getByText(byTextContent("Applicant: alice")).closest(".application-card");
        const reviewingCard = screen.getByText(byTextContent("Applicant: bob")).closest(".application-card");
        expect(appliedCard).toHaveClass("application-card-applied");
        expect(reviewingCard).toHaveClass("application-card-reviewing");
    });

    it("should keep applied and reviewing groups expanded by default and historical groups collapsed", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "alice"},
                        job: {id: 1, title: "Java Developer"}
                    },
                    {
                        id: 11,
                        status: "ACCEPTED",
                        user: {name: "bob"},
                        job: {id: 2, title: "QA Engineer"}
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByRole("button", {name: "Collapse Applied"})).toBeInTheDocument());
        expect(screen.getByRole("button", {name: "Expand Accepted"})).toBeInTheDocument();
        expect(screen.getByText(byTextContent("Applicant: alice"))).toBeInTheDocument();
        expect(screen.queryByText(byTextContent("Applicant: bob"))).not.toBeInTheDocument();
    });

    it("should collapse and expand a status group", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "alice"},
                        job: {id: 1, title: "Java Developer"}
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByRole("button", {name: "Collapse Applied"})).toBeInTheDocument());
        await user.click(screen.getByRole("button", {name: "Collapse Applied"}));
        expect(screen.queryByText(byTextContent("Applicant: alice"))).not.toBeInTheDocument();
        await user.click(screen.getByRole("button", {name: "Expand Applied"}));
        expect(screen.getByText(byTextContent("Applicant: alice"))).toBeInTheDocument();
    });

    it("should move an updated application into the correct status group", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "user"},
                        job: {id: 1, title: "Java Developer"}
                    }
                ]
            });
        axios.put.mockResolvedValueOnce({
            data: {
                id: 10,
                status: "REVIEWING",
                user: {name: "user"},
                job: {id: 1, title: "Java Developer"}
            }
        });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByLabelText("Status")).toBeInTheDocument());
        await user.selectOptions(screen.getByLabelText("Status"), "REVIEWING");
        await user.click(screen.getByRole("button", {name: "Save"}));

        expect(await screen.findByText("Application status updated successfully.")).toBeInTheDocument();
        expect(screen.queryByRole("heading", {name: "Applied (1)"})).not.toBeInTheDocument();
        const reviewingGroup = screen.getByTestId("application-group-reviewing");
        expect(within(reviewingGroup).getByText(byTextContent("Applicant: user"))).toBeInTheDocument();
    });

    it("should sort applications within groups by oldest first", async () => {
        localStorage.setItem("token", "jwt-admin");
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 10,
                        status: "APPLIED",
                        user: {name: "alice"},
                        job: {id: 1, title: "Java Developer"},
                        createdAt: "2026-05-26T10:00:00"
                    },
                    {
                        id: 11,
                        status: "APPLIED",
                        user: {name: "bob"},
                        job: {id: 2, title: "QA Engineer"},
                        createdAt: "2026-05-25T10:00:00"
                    }
                ]
            });

        renderWithProviders(<AdminDashboard />);
        const user = userEvent.setup();

        await openApplicationsTab(user);
        await waitFor(() => expect(screen.getByLabelText("Sort By")).toBeInTheDocument());
        await user.selectOptions(screen.getByLabelText("Sort By"), "oldest");

        const headings = screen.getAllByRole("heading", {level: 4});
        expect(headings[0]).toHaveTextContent("Applicant: bob");
        expect(headings[1]).toHaveTextContent("Applicant: alice");
    });
});
