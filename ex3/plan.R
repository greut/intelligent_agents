#!/usr/bin/env Rscript

require(ggplot2)

d <- read.table("plan0.csv", sep=",")

colnames(d) <- c("vehicle", "distance", "load")
vehicles <- factor(d$vehicle)

p <- ggplot() + scale_y_continuous("load %", limits=c(0, 100))

for (v in levels(vehicles)) {
    dv <- d[d$vehicle == v,]
    if (dim(dv)[1] > 1) {
        p <- p + geom_line(data=dv, aes(distance, load), col=strtoi(v)+2)
    }
}

png("plan.png", width=800, height=200, units="px")
print(p)
dev.off()
