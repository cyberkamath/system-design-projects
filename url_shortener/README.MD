URL shortner project - 2 Microservices

Overview: A URL shortener supporting up to 10K URLs and 100K redirects/day in the homelab. Redirect latency target: <100ms p99. Analytics can lag redirects by up to 60 seconds. Single-user for v1, no auth on the shorten endpoint.

    url service
    Analytics service

    Spec : 

    Endpoints :  
        url service : 
            POST /shorten with url in body
            GET /{code} return redirect 302 if match found or return 404
        analytics service:
            get /dashboard
                info: top 20 percent of resovled urls, number of urls in cachce, total_urls, trend_by_day, trend_by_weeks,top_100_domains_shortend
            get /drilldown/{code}
                info: number of resolutions, time distribuition, referer
            
            probably batch job to do the analytics and pre fill the dashboard details

    Database (postgres):
    url service:
        Own Postgres container on port 5432
        Database: urlshortener
        (existing tables)
    analytics service:
        Own Postgres container on port 5433
        Database: analytics
        (existing tables)
    Database (postgres) :
        url service :
            Own Postgres container on port 5432
            Database: urlshortener
            
            table short_url - id serial or big int (primary key), original_url text, short_url varchar(10) (unique constraint), created_date timestamp

            index on created_date and short_url
        
        analytics service :
            Own Postgres container on port 5433
            Database: analytics 
            
            table records - id serial or big int, original_domain text, short_url varchar(10), ip_address INET ,resolver_node_id char(2), resolve_time_ms int, was_in_cache boolean , referer text, created_date timestamp

            index on created_date and short_url

            retention_days: 30

            table per_url_resolve_count - 
                short_url varchar(10), hitcount bigint
                    
            table per_referer_resolve_count - 
                referer_url text, hitcount bigint
                
            table per_domains_shortened_count - 
                original_domain text, hitcount bigint

            table stats - 
                stat_name text, value text
                total_urls : count of urls shorteand
                total_resolves : total_resolution
    Kafka : 
        event_shape: Json with code, timestamp, ip_address, useragent, referrer, resolver_node_id, was_in_cache, resolve_time_ms

        On publish failure: log and drop (acceptable data loss for v1).
    
    Redis : 
        code to longurl mapping, TTL 1 hour

    Short code generation strategy : generate base62 encoding for the auto generated id and concatenate a 2 digit identifier to add node id as a suffix. Node Id will be hardcoded for homeserver using env variable.

    Non Goals : Custom aliases, Expiration, Analytics on unresolved URLs, Authentication and Authorization,Idempotency, Plugin Architecture , Secuirty testing, DDOS or DOS prevention 

    Future work  for V2 : CI/CD. API gateway, Rate limits, Number of URLS cached in Redis with metrics from Spring boot, trend_by_geography