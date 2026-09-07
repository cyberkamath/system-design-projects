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
                info: top 20 percent of resovled urls, number of urls in cachce, total_urls, trend_by_day, trend_by_weeks,trend_by_geography,top_100_domains_shortend
            get /drilldown/{code}
                info: number of resolutions, geographic distribtuion, time distribuition, referer
            
            probably batch job to do the analytics and pre fill the dashboard details
    Database (postgres) :
        url service (url schema): 
                table short_url - id serial or big int (primary key), original_url text, short_url varchar(10) (unique constraint), created_date timestamp

                index on created_date and short_url
        
        analytics service (analysis schema) : 
                table records - id serial or big int, original_domain text, short_url varchar(10), ip_address INET ,resolver_node_id varchar(3), resolve_time_ms int, was_in_cache boolean , referer text, created_date timestamp

                index on created_date and short_url

                retention_days: 30

                table per_url_resolve_count - 
                    short_url varchar(10), hitcount bigint
                    
                table per_referer_resolve_count - 
                    referer_url text, hitcount bigint
                
                table per_domains_shortened_count - 
                    referer_url text, hitcount bigint

                table stats - 
                    stat_name text, value text
                        total_urls : count of urls shorteand
                        total_resolves : total_resolution
    Kafka : 
        event_shape: Json with code, timestamp, ip_address, useragent, referrer, geographic id or country code, resolving_server_id, was_in_cache, time_taken_to_resolve
    
    Redis : 
        code to longurl mapping, TTL 1 hour

    Short code generation strategy : generate base62 encoding for the auto generated id and concatenate a 2 digit identifier to add node id.

    Non Goals : Custom aliases, Expiration, Analytics on unresolved URLs, Authentication and Authorization , API gateway, Rate limits, Idempotency, Modularity, Extensibility, Secuirty testing, DDOS or DOS prevention, CI/CD.