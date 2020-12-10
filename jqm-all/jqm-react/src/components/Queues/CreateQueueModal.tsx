import React, { useState } from "react";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import { Queue } from "./Queue";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Modal from "@material-ui/core/Modal";

function getModalStyle() {
    const top = 50;
    const left = 50;

    return {
        top: `${top}%`,
        left: `${left}%`,
        transform: `translate(-${top}%, -${left}%)`,
    };
}

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        paper: {
            position: "absolute",
            width: 400,
            backgroundColor: theme.palette.background.paper,
            border: "2px solid #000",
            boxShadow: theme.shadows[5],
            padding: theme.spacing(2, 4, 3),
        },
    })
);

export const CreateQueueModal: React.FC<{
    showModal: boolean;
    setShowModal: (bool: boolean) => void;
    onCreate: (queue: Queue) => void;
}> = ({ showModal, setShowModal, onCreate }) => {
    const [queueName] = useState<string>();
    const [description] = useState<string>();
    const [defaultQueue] = useState(false);

    const handleOnClose = () => setShowModal(false);

    const classes = useStyles();
    // getModalStyle is not a pure function, we roll the style only on the first render
    const [modalStyle] = React.useState(getModalStyle);

    const body = (
        <div style={modalStyle} className={classes.paper}>
            <h2 id="simple-modal-title">Text in a modal</h2>
            <AddCircleIcon
                onClick={() => {
                    if (queueName) {
                        console.log("click");
                        onCreate(
                            new Queue(queueName, defaultQueue, description)
                        );
                    }
                }}
            />
        </div>
    );

    return (
        <Modal
            open={showModal}
            onClose={handleOnClose}
            aria-labelledby="simple-modal-title"
            aria-describedby="simple-modal-description"
        >
            {body}
        </Modal>
    );
};
