#!/usr/bin/env python3
"""Regression tests for namespace migration rules."""
import unittest

from migrate_package_namespace import (
    DEPENDENCY_COORDINATE,
    EXTERNAL_TREESITTER_IMPORT,
    LEGACY_TREESITTER_IMPORT,
    NEW_DOTTED,
    OLD_DOTTED,
)


class NamespaceMigrationRulesTest(unittest.TestCase):
    def test_published_treesitter_runtime_import_keeps_upstream_package(self):
        source = f"import {NEW_DOTTED}.treesitter.TSNode\n"
        migrated = EXTERNAL_TREESITTER_IMPORT.sub(
            lambda match: match.group(1) + OLD_DOTTED + ".treesitter.", source
        )
        self.assertEqual(migrated, f"import {OLD_DOTTED}.treesitter.TSNode\n")

    def test_our_treesitter_api_wrapper_stays_in_new_namespace(self):
        source = f"import {NEW_DOTTED}.treesitter.api.TreeSitterNode\n"
        migrated = EXTERNAL_TREESITTER_IMPORT.sub(
            lambda match: match.group(1) + OLD_DOTTED + ".treesitter.", source
        )
        self.assertEqual(migrated, source)

    def test_published_dependency_coordinates_are_not_renamed(self):
        source = f"{NEW_DOTTED}.treesitter:runtime:1.0"
        restored = DEPENDENCY_COORDINATE.sub(
            lambda match: match.group(0).replace(NEW_DOTTED, OLD_DOTTED),
            source,
        )
        self.assertEqual(restored, f"{OLD_DOTTED}.treesitter:runtime:1.0")


if __name__ == "__main__":
    unittest.main()
