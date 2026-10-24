 Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

2.0.1-SNAPSHOT - 2026-10-24

Fixed

- Qualify the ASRH membership closure query's enrollment status column so joined
  family and member status columns do not cause an ambiguous-column SQL error.
- Preserve active enrollment selection, member details and older members needed
  by the age-based closure job, with focused SQLite regression coverage.

