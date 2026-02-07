import {useDispatch, useSelector} from "react-redux";
import {Link, useNavigate} from "react-router-dom";
import {selectUser} from "../store/userSelectors";
import {setUserDetails} from "../store/userActions";

const Navbar = () => {
    const user = useSelector(selectUser);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.clear();
        let initialuserState = {
            username: "",
            role: ""
        }
        setUserDetails(dispatch, initialuserState)
        navigate("/")

    }
    return (
        <>
            <nav className="navbar navbar-expand-lg navbar-light bg-light">
                <div className="container">
                    <Link className="navbar-brand" to="/">
                        <strong>Job Portal</strong>
                    </Link>

                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav ms-auto">
                            <li className="nav-item">
                                <span className="nav-link">Welcome, {user.username}</span>
                            </li>
                            <li className="nav-item">
                                <button className="btn btn-danger ms-3" onClick={handleLogout}>
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
