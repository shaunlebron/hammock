## 0.2.0

### changes

- `old-tree` renamed to `src-tree`
- `new-tree` renamed to `dst-tree`
- add `result` function for retrieving destination tree with `:anchors` metadata

## 0.1.1

### bug fixes

- `old-tree` no longer tries to deref its map

## 0.1.0

### initial features

- `create` create a new hammock
- `copy!` set new tree based on old tree
- `nest!` move hammock to separate branches and execute function
- `map!` map an old sequence to a new sequence with a map function receiving a nested hammock
- `man!` manually set new tree value, and manually set related branches
- `ILookup` read from old tree at the given hammock
- `old-tree` get the hammock's entire old tree
- `new-tree` get the hammock's entire new transformed tree
- `anchors` get the hammock's map of old<->new related branches
