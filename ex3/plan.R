#!/usr/bin/env Rscript

require(ggplot2)

d <- read.table("plan.csv", sep=",")

colnames(d) <- c("vehicle", "distance", "load")


p <- ggplot() +
     geom_line(data=d[d$vehicle == 0,], aes(distance, load), colour="red") +
     geom_line(data=d[d$vehicle == 1,], aes(distance, load), colour="blue") +
     geom_line(data=d[d$vehicle == 2,], aes(distance, load), colour="green") +
     geom_line(data=d[d$vehicle == 3,], aes(distance, load), colour="cyan")

png("plan.png", width=800, height=300, units="px")
print(p)
dev.off()
