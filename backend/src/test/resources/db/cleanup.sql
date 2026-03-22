-- Test cleanup script - truncates all tables to ensure clean state
-- This script is executed after test suites to rollback database changes

TRUNCATE TABLE issues RESTART IDENTITY CASCADE;
