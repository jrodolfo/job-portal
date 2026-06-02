import {createRef} from "react";
import {render, screen} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import AdminDisableUserModal from "./AdminDisableUserModal";

describe("AdminDisableUserModal", () => {
    it("should render the disable warning and wire modal actions", async () => {
        const user = userEvent.setup();
        const cancelButtonRef = createRef();
        const onCancel = vi.fn();
        const onConfirm = vi.fn();

        render(
            <AdminDisableUserModal
                userLabel="Rod Oliveira"
                cancelButtonRef={cancelButtonRef}
                onCancel={onCancel}
                onConfirm={onConfirm}
            />
        );

        expect(screen.getByRole("dialog", {name: "Disable Rod Oliveira?"})).toBeInTheDocument();
        expect(screen.getByText("This user will be signed out and will not be able to log in.")).toBeInTheDocument();
        expect(cancelButtonRef.current).toBe(screen.getByRole("button", {name: "Cancel"}));

        await user.click(screen.getByRole("button", {name: "Close disable dialog"}));
        expect(onCancel).toHaveBeenCalledTimes(1);

        await user.click(screen.getByRole("button", {name: "Cancel"}));
        expect(onCancel).toHaveBeenCalledTimes(2);

        await user.click(screen.getByRole("button", {name: "Disable User"}));
        expect(onConfirm).toHaveBeenCalledTimes(1);
    });
});
