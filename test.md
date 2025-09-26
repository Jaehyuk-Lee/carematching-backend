./gradlew runLoadTestNoPool --no-daemon --console=plain
### Redis 부하테스트 with No Pool
NoPool stats: count=200 errors=0 avg=4762.614ms min=4719.734ms max=4782.970ms p50=4767.205ms p95=4780.906ms p99=4782.451ms
NoPool stats: count=553 errors=0 avg=2548.200ms min=92.100ms max=4782.970ms p50=1849.319ms p95=4780.118ms p99=4781.425ms
NoPool stats: count=1041 errors=0 avg=1911.582ms min=84.167ms max=4782.970ms p50=1565.107ms p95=4778.199ms p99=4780.865ms
NoPool stats: count=1506 errors=0 avg=1716.077ms min=84.167ms max=4782.970ms p50=1521.525ms p95=4773.117ms p99=4780.814ms
NoPool stats: count=1924 errors=0 avg=1639.473ms min=33.657ms max=4782.970ms p50=1503.286ms p95=4767.899ms p99=4780.706ms
NoPool stats: count=2447 errors=0 avg=1598.382ms min=33.657ms max=4782.970ms p50=1536.872ms p95=4758.715ms p99=4780.344ms
NoPool stats: count=2963 errors=0 avg=1560.107ms min=33.657ms max=4782.970ms p50=1499.169ms p95=4749.637ms p99=4780.068ms
NoPool stats: count=3575 errors=0 avg=1537.969ms min=33.657ms max=4782.970ms p50=1489.481ms p95=4737.541ms p99=4779.659ms

./gradlew runLoadTestReuse --no-daemon --console=plain -Pargs="127.0.0.1 6379 200 200 30"
### Redis 부하테스트 with Pool
Reuse-interval stats: count=43619 errors=0 avg=23.079ms min=8.711ms max=145.441ms p50=20.453ms p95=42.638ms p99=55.655ms
Reuse-interval stats: count=48670 errors=0 avg=41.265ms min=8.682ms max=56.389ms p50=19.546ms p95=29.502ms p99=42.302ms
Reuse-interval stats: count=51363 errors=0 avg=58.537ms min=8.366ms max=48.860ms p50=18.711ms p95=27.031ms p99=31.497ms
Reuse-interval stats: count=50154 errors=0 avg=79.943ms min=7.668ms max=49.609ms p50=19.347ms p95=27.204ms p99=32.761ms
Reuse-interval stats: count=48156 errors=0 avg=103.954ms min=8.699ms max=60.749ms p50=19.465ms p95=32.948ms p99=43.616ms
