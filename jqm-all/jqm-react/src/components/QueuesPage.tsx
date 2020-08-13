import React, { useEffect, useState } from 'react';
import { Typography, Container } from '@material-ui/core';
import APIService from '../utils/APIService';

const QueuesPage: React.FC = () => {
    const [queues, setQueues] = useState<any[] | null>();

    useEffect(() => {
        APIService.get("/q")
            .then((response) => { setQueues(response) })
            .catch((reason) => console.log(reason));
    }, [])
    return (<Container>
        <Typography variant="h5">
            Welcome to the queues yo
        </Typography>
        {queues?.map((queue) => (<Typography key={queue.id}>- {queue.name}</Typography>))}
    </Container>);

}

export default QueuesPage
