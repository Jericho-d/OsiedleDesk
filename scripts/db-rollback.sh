#!/bin/bash
# Database rollback script - resets PostgreSQL database to initial state
# This script is designed to run inside a Docker container

set -e

echo "Starting database rollback..."

# Wait for PostgreSQL to be ready
until pg_isready -h postgres -p 5432 -U admtool; do
    echo "Waiting for PostgreSQL..."
    sleep 2
done

echo "PostgreSQL is ready. Rolling back database..."

# Drop and recreate the database
psql -h postgres -U admtool -d postgres -c "DROP DATABASE IF EXISTS admtool;"
psql -h postgres -U admtool -d postgres -c "CREATE DATABASE admtool;"

echo "Database rollback complete. Database 'admtool' has been reset to initial state."
