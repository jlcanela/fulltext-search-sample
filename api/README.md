Frontend Based on 
- https://slickgrid.net/

Using GraphQL query like:
```
query {
  logs(first: 0, size: 5) {
      datetime ip uri 
  }  
  logsCount
}
```
