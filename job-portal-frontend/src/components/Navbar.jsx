import {useDispatch, useSelector} from "react-redux";
import {Link, useNavigate} from "react-router-dom";
import {selectUser} from "../store/userSelectors";
import {setUserDetails} from "../store/userActions";

/**
 * Navbar component displays the application brand and user information, including a logout button.
 *
 * @returns {JSX.Element} The rendered Navbar component.
 */
const Navbar = () => {
    const user = useSelector(selectUser);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.clear();
        let initialuserState = {
            username: "",
            displayName: "",
            role: ""
        }
        setUserDetails(dispatch)(initialuserState)
        navigate("/")

    }
    return (
        <>
            <nav className="navbar navbar-expand-lg app-navbar">
                <div className="container">
                    <Link className="navbar-brand brand-mark" to="/">
                        <strong>Job Portal</strong>
                    </Link>

                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav ms-auto">
                            <li className="nav-item">
                                <span className="nav-link welcome-text body-text">Welcome, {user.displayName || user.username}</span>
                            </li>
                            <li className="nav-item">
                                <button className="btn btn-accent-primary ms-3" onClick={handleLogout}>
                                    Logout
                                </button>
                            </li>

                        </ul>
                    </div>
                </div>
            </nav>
        </>
    )
}

export default Navbar;
