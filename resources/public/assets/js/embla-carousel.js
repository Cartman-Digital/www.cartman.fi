const emblaNode = document.querySelector('.embla')
const options = { loop: true }
const plugins = [EmblaCarouselAutoplay()]
const viewportNode = emblaNode.querySelector('.embla__viewport')
const emblaApi = EmblaCarousel(viewportNode, options,plugins) 

console.log(emblaApi.slideNodes()) // Access API


