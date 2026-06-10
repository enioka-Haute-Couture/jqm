import React, { useCallback, useEffect, useState } from "react";
import { Box, Card, CardContent, Container, Link, ToggleButton, ToggleButtonGroup, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import APIService from "../utils/APIService";
import { useNotificationService } from "../utils/NotificationService";
import { setPageTitle } from "../utils/title";

interface UsageStats {
    activeNodes: number;
    queues: number;
    pausedJobs: number;
    submittedJobs: number;
    runningJobs: number;
}

interface HistorySlot {
    slotStart: string;
    ended: number;
    crashed: number;
    killed: number;
    cancelled: number;
}

const UsageStatsCard: React.FC<{ label: string, value: any, color?: string }> = ({ label, value, color }) => {
    return (
        <Card sx={{ width: 250, height: 150 }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography variant="h5" sx={{ textAlign: 'center' }}>{label}</Typography>
                <Typography variant="h4" sx={{ fontWeight: 'bold', marginTop: 2, textAlign: 'center', color: color }}>{value}</Typography>
            </CardContent>
        </Card>
    );
}

const formatSlotLabel = (slotStart: string, hours: number): string => {
    const date = new Date(slotStart);
    if (hours === 1) {
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const HomePage: React.FC = () => {
    const { t } = useTranslation();
    const [documentationLink, setDocumentationLink] = React.useState("http://jqm.readthedocs.org/en/master");
    const { displayError } = useNotificationService();

    const [usageStats, setUsageStats] = React.useState<UsageStats>();
    const [historySlots, setHistorySlots] = useState<HistorySlot[]>([]);
    const [period, setPeriod] = useState<1 | 24>(24);

    const fetchDashboardData = useCallback(
        async () => {
            try {
                const [usage, history] = await Promise.all([
                    APIService.get("/admin/stats/usage"),
                    APIService.get(`/admin/stats/history?hours=${period}`),
                ]);
                setUsageStats(usage);
                setHistorySlots(history);
            } catch (e) {
                displayError(e);
            }
        },
        [period, displayError]
    );

    useEffect(() => {
        fetchDashboardData();

        let timer = window.setInterval(() => {
            fetchDashboardData();
        }, 10_000);

        return () => {
            if (timer !== null) clearInterval(timer);
        };
    }, [fetchDashboardData]);

    useEffect(() => {
        APIService.get("/admin/version").then((data) => {
            setDocumentationLink(`http://jqm.readthedocs.org/en/jqm-all-${data.mavenVersion}/`);
        });

        setPageTitle("Home");
    }, []);

    const chartData = historySlots.map(slot => ({
        ...slot,
        label: formatSlotLabel(slot.slotStart, period),
    }));

    return (
        <Container sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4, marginTop: 2 }}>
            <Box sx={{ textAlign: 'left', width: '800px' }}>
                <Typography variant="h5">
                    {t("home.title")}
                </Typography>
                <Typography variant="body1">
                    {t("home.helpText")}
                </Typography>
                <Typography variant="body1">
                    {t("home.documentationText")} <Link href={documentationLink}>{t("home.documentationLink")}</Link> {t("home.documentationSuffix")}
                </Typography>
            </Box>

            {usageStats && (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '800px' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                        <UsageStatsCard label={t("home.usageStats.activeNodes")} value={usageStats.activeNodes} />
                        <UsageStatsCard label={t("home.usageStats.queues")} value={usageStats.queues} />
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <UsageStatsCard label={t("home.usageStats.runningJobs")} value={usageStats.runningJobs} color={`${usageStats.runningJobs === 0 ? '#ff8c00' : '#28a745'}`} />
                        <UsageStatsCard label={t("home.usageStats.submittedJobs")} value={usageStats.submittedJobs} color={`${usageStats.submittedJobs > 0 ? '#ff8c00' : '#28a745'}`} />
                        <UsageStatsCard label={t("home.usageStats.pausedJobs")} value={usageStats.pausedJobs} color={`${usageStats.pausedJobs > 0 ? '#ff8c00' : '#28a745'}`} />
                    </Box>
                </Box>)
            }

            {historySlots.length > 0 && (
                <Box sx={{ width: '800px', marginRight: 6 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 1 }}>
                        <Typography variant="h6" style={{ paddingLeft: "32px" }}>{t("home.historyChart.title")}</Typography>
                        <ToggleButtonGroup
                            value={period}
                            exclusive
                            size="small"
                            onChange={(_e, v) => { if (v !== null) setPeriod(v); }}
                        >
                            <ToggleButton value={1}>{t("home.historyChart.last1h")}</ToggleButton>
                            <ToggleButton value={24}>{t("home.historyChart.last24h")}</ToggleButton>
                        </ToggleButtonGroup>
                    </Box>
                    <ResponsiveContainer width="100%" height={280}>
                        <BarChart data={chartData} margin={{ top: 4, right: 8, left: 0, bottom: 4 }}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="label" tick={{ fontSize: 11 }} interval="preserveStartEnd" />
                            <YAxis allowDecimals={false} />
                            <Tooltip />
                            <Legend />
                            <Bar dataKey="ended" stackId="a" fill="#4caf50" name="Ended" />
                            <Bar dataKey="crashed" stackId="a" fill="#f44336" name="Crashed" />
                            <Bar dataKey="killed" stackId="a" fill="#ff9800" name="Killed" />
                            <Bar dataKey="cancelled" stackId="a" fill="#9e9e9e" name="Cancelled" />
                        </BarChart>
                    </ResponsiveContainer>
                </Box>)}
        </Container>
    );
};

export default HomePage;
