import React, { useState } from "react";
import { Queue } from "./Queue";
import {
    Button,
    Switch,
} from "@material-ui/core";
import { makeStyles, Theme, createStyles } from "@material-ui/core/styles";
import Modal from "@material-ui/core/Modal";
import TextField from "@material-ui/core/TextField/TextField";
import FormControlLabel from '@material-ui/core/FormControlLabel';



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
    closeModal: any;
    createQueue: (queue: Queue) => void;
}> = ({ showModal, closeModal, createQueue }) => {
    const [queueName, setQueueName] = useState<string>("");
    const [description, setDescription] = useState<string>("");
    const [defaultQueue, setDefaultQueue] = useState(false);


    const classes = useStyles();
    // getModalStyle is not a pure function, we roll the style only on the first render
    const [modalStyle] = React.useState(getModalStyle);

    const body = (
        <div style={modalStyle} className={classes.paper}>
            <h2 id="simple-modal-title">Create queue</h2>
            <form noValidate autoComplete="off">
                <TextField
                    label="Name"
                    value={queueName}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setQueueName(event.target.value);
                    }}
                    fullWidth />
                <TextField label="Description"
                    value={description}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        setDescription(event.target.value);
                    }}
                    fullWidth />
                <FormControlLabel
                    control={
                        <Switch
                            checked={defaultQueue}
                            onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                                setDefaultQueue(event.target.checked);
                            }} />
                    }
                    label="Default queue"
                />

            </form>
            <Button variant="contained" size="small" onClick={closeModal} style={{ margin: "8px" }}>Cancel</Button>
            <Button variant="contained" size="small" color="primary" disabled={!queueName || !description}
                style={{ margin: "8px" }}
                onClick={() => {
                    createQueue({ name: queueName!, description: description, defaultQueue: defaultQueue });
                }}>
                Create
            </Button>
        </div>
    );

    return (
        <Modal
            open={showModal}
            onClose={closeModal} // TODO: fix this, doesn't work
            aria-labelledby="simple-modal-title"
        >
            {body}
        </Modal>
    );
};
